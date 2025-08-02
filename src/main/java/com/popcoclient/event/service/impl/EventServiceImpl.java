package com.popcoclient.event.service.impl;

import com.popcoclient.event.dto.request.QuizSubmissionResultDto;
import com.popcoclient.event.dto.response.*;
import com.popcoclient.event.entity.*;
import com.popcoclient.event.entity.key.QuizOptionId;
import com.popcoclient.event.entity.key.QuizQuestionId;
import com.popcoclient.event.repository.*;
import com.popcoclient.exception.BusinessException;
import com.popcoclient.exception.ErrorCode;
import com.popcoclient.user.entity.User;
import com.popcoclient.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
@Transactional
@Slf4j
public class EventServiceImpl {

    // ===== 의존성 주입 =====
    private final DistributeLockServiceImpl lockService;
    private final @Qualifier("eventRedisTemplate") RedisTemplate<String, String> eventRedisTemplate;
    private final RedisScript<Long> submitAnswerScript;
    private final UserQuizAnswerRepository userQuizAnswerRepository;
    private final UserQuizAttemptRepository userQuizAttemptRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizOptionRepository quizOptionRepository;
    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final TaskScheduler taskScheduler;

    // ===== 타이머 관리 (메모리) =====
    private final Map<String, Long> activeTimers = new ConcurrentHashMap<>(); // key: "quizId:questionId", value: 시작시간

    public EventServiceImpl(
            DistributeLockServiceImpl lockService,
            @Qualifier("eventRedisTemplate") RedisTemplate<String, String> eventRedisTemplate,
            UserQuizAnswerRepository userQuizAnswerRepository,
            UserQuizAttemptRepository userQuizAttemptRepository,
            QuizQuestionRepository quizQuestionRepository,
            QuizOptionRepository quizOptionRepository,
            UserRepository userRepository,
            QuizRepository quizRepository,
            SimpMessagingTemplate messagingTemplate,
            TaskScheduler taskScheduler
    ) {
        this.lockService = lockService;
        this.eventRedisTemplate = eventRedisTemplate;
        this.userQuizAnswerRepository = userQuizAnswerRepository;
        this.userQuizAttemptRepository = userQuizAttemptRepository;
        this.quizQuestionRepository = quizQuestionRepository;
        this.quizOptionRepository = quizOptionRepository;
        this.userRepository = userRepository;
        this.quizRepository = quizRepository;
        this.messagingTemplate = messagingTemplate;
        this.taskScheduler = taskScheduler;
        this.submitAnswerScript = createSubmitAnswerScript();
    }

    public Long getQuizId(){
     Long latest = quizRepository.findMaxId();
     return latest;
    }

    public QuizQuestionResponseDto getQuizQuestion(Long quizId, Long questionId){
        QuizQuestionId quizQuestionId = QuizQuestionId.of(questionId, quizId);
        QuizQuestion quizQuestion = quizQuestionRepository.findById(quizQuestionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUESTION_NOT_FOUND));
        List<QuizOption> quizOption = quizQuestion.getOptions();
        List<QuizQuestionsOptionsResponseDto> optionsList = new ArrayList<>();

        for (QuizOption quizOptionDto : quizOption) {
            QuizQuestionsOptionsResponseDto optionsDto = new QuizQuestionsOptionsResponseDto(quizOptionDto.getContent(), quizOptionDto.getIsCorrect());
            optionsList.add(optionsDto);
        }

        return QuizQuestionResponseDto.builder()
                .quizId(quizId)
                .questionId(questionId)
                .content(quizQuestion.getContent())
                .firstCapacity(quizQuestion.getFirstCapacity())
                .options(optionsList)
                .build();
    }

    // ===== 📋 1. 답안 제출 API =====
    public QuizSubmissionResultDto submitAnswer(Long quizId, Long questionId, Long userId, Long optionId) {
        long startTime = System.currentTimeMillis();
        log.info("Answer submission started - quizId: {}, questionId: {}, userId: {}, optionId: {}",
                quizId, questionId, userId, optionId);

        // 🔍 1단계: 기본 엔티티 조회 및 검증
        QuizValidationResultDto validation = validateAndLoadEntities(quizId, questionId, userId, optionId);
        if (!validation.isValid()) {
            return createErrorResult(validation.getErrorStatus(), validation.getErrorMessage());
        }

        QuizQuestion question = validation.getQuestion();
        QuizOption selectedOption = validation.getSelectedOption();
        UserQuizAttempt attempt = validation.getAttempt();

        // 🔐 2단계: 중복 제출 방지 락
        String duplicateLockKey = DistributeLockServiceImpl.LockKeys.participantAnswer(questionId, userId);
        String lockValue = UUID.randomUUID().toString();

        if (!lockService.tryLock(duplicateLockKey, lockValue, 30)) {
            log.warn("Duplicate submission attempt - questionId: {}, userId: {}", questionId, userId);
            return QuizSubmissionResultDto.duplicate();
        }

        try {
            // 🔍 3단계: 이미 답했는지 확인
            QuizQuestionId questionKey = QuizQuestionId.of(questionId, quizId);
            boolean alreadyAnswered = userQuizAnswerRepository.existsByAttemptAndQuestionId(attempt, questionKey);
            if (alreadyAnswered) {
                return QuizSubmissionResultDto.alreadySubmitted();
            }

            // ✅ 4단계: 정답 여부 확인
            if (!selectedOption.getIsCorrect()) {
                saveWrongAnswer(attempt, selectedOption);
                return QuizSubmissionResultDto.wrongAnswer();
            }

            // 🏁 5단계: 정답자 선착순 처리
            QuizSubmissionResultDto result = processCorrectAnswer(question, selectedOption, attempt, startTime);

            // 🚀 6단계: 통과자라면 다음 문제 준비 체크
            if (result.isSurvived()) {
                checkAndStartNextQuestion(quizId, questionId);
            }

            return result;

        } finally {
            lockService.unlock(duplicateLockKey, lockValue);
        }
    }

    // ===== 📊 2. 퀴즈 상태 조회 API =====
    public QuizStatusResponseDto getQuizStatus(Long quizId, Long questionId) {
        try {
            // QuizQuestion 조회
            QuizQuestionId questionKey = QuizQuestionId.of(questionId, quizId);
            QuizQuestion question = quizQuestionRepository.findById(questionKey)
                    .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));

            // 현재 생존자 수 조회
            int currentSurvivors = getTotalSurvivors(questionId);
            int maxSurvivors = question.getFirstCapacity();
            double progressPercentage = maxSurvivors > 0 ?
                    (double) currentSurvivors / maxSurvivors * 100.0 : 0.0;

            // 🕒 타이머 상태 계산 (메모리에서)
            String timerKey = quizId + ":" + questionId;
            Long startTime = activeTimers.get(timerKey);

            boolean isActive = false;
            int remainingTime = 0;

            if (startTime != null) {
                long elapsed = System.currentTimeMillis() - startTime;
                remainingTime = Math.max(0, 30 - (int)(elapsed / 1000)); // 30초에서 경과시간 빼기
                isActive = remainingTime > 0;
            }

            return QuizStatusResponseDto.builder()
                    .quizId(quizId)
                    .questionId(questionId)
                    .currentSurvivors(currentSurvivors)
                    .maxSurvivors(maxSurvivors)
                    .isActive(isActive)
                    .remainingTime(remainingTime)
                    .build();

        } catch (Exception e) {
            log.error("Failed to get quiz status - quizId: {}, questionId: {}", quizId, questionId, e);
            throw new RuntimeException("퀴즈 상태 조회에 실패했습니다.", e);
        }
    }

    // ===== 📈 3. 실시간 브로드캐스트 API =====
    public void broadcastSurvivorUpdate(Long quizId, Long questionId, int latestRank, int totalSurvivors) {
        try {
            QuizStatusResponseDto currentStatus = getQuizStatus(quizId, questionId);
            String questionTopic = "/topic/quiz/" + quizId + "/question/" + questionId;
            messagingTemplate.convertAndSend(questionTopic, currentStatus);

            log.debug("Broadcasted progress update - topic: {}, survivors: {}/{}",
                    questionTopic, currentStatus.getCurrentSurvivors(), currentStatus.getMaxSurvivors());

        } catch (Exception e) {
            log.error("Failed to broadcast survivor update - quizId: {}, questionId: {}", quizId, questionId, e);
        }
    }

    // ===== 🏆 4. 생존자 순위 조회 API =====
    public SurvivorRankingResponse getSurvivorRanking(Long quizId, Long questionId, int page, int size) {
        try {
            String survivorKey = RedisKeys.survivorRanking(questionId);
            long start = (long) page * size;
            long end = start + size - 1;

            Set<String> userIds = eventRedisTemplate.opsForZSet().range(survivorKey, start, end);
            List<SurvivorRankingResponse.SurvivorInfo> survivors = new ArrayList<>();

            if (userIds != null) {
                int rank = (int) start + 1;
                for (String userIdStr : userIds) {
                    Long userId = Long.parseLong(userIdStr);
                    Long submissionTime = eventRedisTemplate.opsForZSet().score(survivorKey, userIdStr).longValue();

                    survivors.add(SurvivorRankingResponse.SurvivorInfo.builder()
                            .userId(userId)
                            .rank(rank++)
                            .submissionTime(submissionTime)
                            .responseTimeMs(0L)
                            .build());
                }
            }

            int totalSurvivors = getTotalSurvivors(questionId);
            int totalPages = (int) Math.ceil((double) totalSurvivors / size);

            return SurvivorRankingResponse.builder()
                    .quizId(quizId)
                    .questionId(questionId)
                    .totalSurvivors(totalSurvivors)
                    .currentPage(page)
                    .totalPages(totalPages)
                    .survivors(survivors)
                    .build();

        } catch (Exception e) {
            log.error("Failed to get survivor ranking - quizId: {}, questionId: {}", quizId, questionId, e);
            throw new RuntimeException("생존자 순위 조회에 실패했습니다.", e);
        }
    }

    // ===== 🚀 핵심 내부 로직들 =====

    /**
     * 다음 문제 준비 체크 및 시작
     */
    private void checkAndStartNextQuestion(Long quizId, Long questionId) {


        // 🎯 현재 문제의 정원 확인
        QuizQuestionId currentQuestionKey = QuizQuestionId.of(questionId, quizId);
        QuizQuestion currentQuestion = quizQuestionRepository.findById(currentQuestionKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));

        int currentSurvivors = getTotalSurvivors(questionId);
        int currentCapacity = currentQuestion.getFirstCapacity();

        // 🚀 현재 문제 정원이 다 찼으면 다음 문제 시작
        if (currentSurvivors >= currentCapacity) {
            Long nextQuestionId = questionId + 1;

            // 다음 문제가 존재하는지 확인
            QuizQuestionId nextQuestionKey = QuizQuestionId.of(nextQuestionId, quizId);
            if (!quizQuestionRepository.existsById(nextQuestionKey)) {
                log.info("마지막 문제 완료! 퀴즈 종료");
                return;
            }

            String timerKey = quizId + ":" + nextQuestionId;

            // 이미 시작된 문제는 중복 시작 방지
            if (!activeTimers.containsKey(timerKey)) {
                log.info("문제 {} 정원 달성({}/{})! 다음 문제 {} 5초 후 시작",
                        questionId, currentSurvivors, currentCapacity, nextQuestionId);

                // 🕒 5초 후 다음 문제 시작
                taskScheduler.schedule(() -> {
                    startQuestion(quizId, nextQuestionId);
                }, Instant.now().plus(5, ChronoUnit.SECONDS));
            }
        }
    }

    /**
     * 문제 시작 (5초 후 실행)
     */
    private void startQuestion(Long quizId, Long questionId) {
        long questionStartTime = System.currentTimeMillis(); // 이 순간이 문제 시작시간!
        String timerKey = quizId + ":" + questionId;

        // 메모리에 시작시간 저장
        activeTimers.put(timerKey, questionStartTime);
        log.info("문제 {} 시작! 30초 타이머 가동", questionId);

        // 🆕 1초마다 타이머 브로드캐스트 시작
        ScheduledFuture<?> timerBroadcast = taskScheduler.scheduleAtFixedRate(() -> {
            try {
                QuizStatusResponseDto currentStatus = getQuizStatus(quizId, questionId);
                String questionTopic = "/topic/quiz/" + quizId + "/question/" + questionId;
                messagingTemplate.convertAndSend(questionTopic, currentStatus);

                log.debug("Timer broadcast - topic: {}, remaining: {}s",
                        questionTopic, currentStatus.getRemainingTime());
            } catch (Exception e) {
                log.error("Failed to broadcast timer - quizId: {}, questionId: {}", quizId, questionId, e);
            }
        }, Instant.now().plus(1, ChronoUnit.SECONDS), Duration.ofSeconds(1));

        // 🕒 30초 후 자동 종료
        taskScheduler.schedule(() -> {
            activeTimers.remove(timerKey);
            log.info("문제 {} 시간 종료! 타이머 제거됨", questionId);

            // 타이머 종료 브로드캐스트
            broadcastQuestionTimeout(quizId, questionId);

        }, Instant.now().plus(30, ChronoUnit.SECONDS));

        // 문제 시작 브로드캐스트
        broadcastQuestionStart(quizId, questionId);
    }

    /**
     * 🎯 문제1 시작 (퀴즈 시작 버튼으로 즉시 시작)
     */
    public void startFirstQuestion(Long quizId) {
        Long questionId = 1L;
        String timerKey = quizId + ":" + questionId;

        // 이미 시작되었으면 중복 방지
        if (activeTimers.containsKey(timerKey)) {
            log.warn("문제1 이미 시작됨 - quizId: {}", quizId);
            return;
        }

        log.info("문제1 시작! 즉시 타이머 가동 - quizId: {}", quizId);

        // 바로 문제 시작 (5초 대기 없음)
        startQuestion(quizId, questionId);
    }

    /**
     * 문제 시작 브로드캐스트
     */
    private void broadcastQuestionStart(Long quizId, Long questionId) {
        try {
            String questionTopic = "/topic/quiz/" + quizId + "/question/" + questionId;
            Map<String, Object> startMessage = Map.of(
                    "type", "QUESTION_START",
                    "quizId", quizId,
                    "questionId", questionId,
                    "message", "문제가 시작되었습니다! 30초의 시간이 주어집니다."
            );
            messagingTemplate.convertAndSend(questionTopic, startMessage);

        } catch (Exception e) {
            log.error("Failed to broadcast question start - quizId: {}, questionId: {}", quizId, questionId, e);
        }
    }

    /**
     * 문제 타임아웃 브로드캐스트
     */
    private void broadcastQuestionTimeout(Long quizId, Long questionId) {
        try {
            String questionTopic = "/topic/quiz/" + quizId + "/question/" + questionId;
            Map<String, Object> timeoutMessage = Map.of(
                    "type", "QUESTION_TIMEOUT",
                    "quizId", quizId,
                    "questionId", questionId,
                    "message", "시간이 종료되었습니다!"
            );
            messagingTemplate.convertAndSend(questionTopic, timeoutMessage);

        } catch (Exception e) {
            log.error("Failed to broadcast question timeout - quizId: {}, questionId: {}", quizId, questionId, e);
        }
    }

    /**
     * 정답자 선착순 처리
     */
    private QuizSubmissionResultDto processCorrectAnswer(QuizQuestion question, QuizOption selectedOption,
                                                         UserQuizAttempt attempt, long startTime) {
        Long questionId = question.getQuestionId().getQuestionId();
        Long quizId = question.getQuestionId().getQuizId();
        Long userId = attempt.getUser().getUserId();
        int firstCapacity = question.getFirstCapacity();
        long submissionTime = System.currentTimeMillis();

        // 🚀 Redis Lua Script로 선착순 순서 결정
        List<String> keys = Arrays.asList(
                RedisKeys.survivorRanking(questionId),
                RedisKeys.submissionCount(questionId),
                RedisKeys.questionProgress(questionId)
        );

        List<String> args = Arrays.asList(
                userId.toString(),
                String.valueOf(submissionTime),
                String.valueOf(firstCapacity),
                String.valueOf(startTime)
        );

        Long qualificationOrder = eventRedisTemplate.execute(submitAnswerScript, keys, args.toArray());
        if (qualificationOrder == null) {
            return QuizSubmissionResultDto.error("시스템 오류가 발생했습니다.");
        }

        // DB에 정답 저장
        boolean advanced = (qualificationOrder > 0 && qualificationOrder <= firstCapacity);
        saveCorrectAnswer(attempt, selectedOption, qualificationOrder, advanced);

        if (advanced) {
            int totalSurvivors = getTotalSurvivors(questionId);
            broadcastSurvivorUpdate(quizId, questionId, qualificationOrder.intValue(), totalSurvivors);
            return QuizSubmissionResultDto.survived(qualificationOrder.intValue(), totalSurvivors);
        } else {
            int finalSurvivors = Math.min(getTotalSurvivors(questionId), firstCapacity);
            return QuizSubmissionResultDto.tooLate(finalSurvivors);
        }
    }

    /**
     * 현재 생존자 수 조회
     */
    private int getTotalSurvivors(Long questionId) {
        String key = RedisKeys.survivorRanking(questionId);
        try {
            Long count = eventRedisTemplate.opsForZSet().zCard(key);
            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
            log.error("Failed to get survivor count for questionId: {}", questionId, e);
            return 0;
        }
    }

    // ===== 🛠️ 헬퍼 메서드들 =====

    private QuizSubmissionResultDto createErrorResult(QuizSubmissionResultDto.SubmissionStatus status, String message) {
        return QuizSubmissionResultDto.builder()
                .status(status)
                .message(message)
                .survived(false)
                .submissionTime(System.currentTimeMillis())
                .build();
    }

    private QuizValidationResultDto validateAndLoadEntities(Long quizId, Long questionId, Long userId, Long optionId) {
        // User 조회
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return QuizValidationResultDto.invalid(
                    QuizSubmissionResultDto.SubmissionStatus.SYSTEM_ERROR,
                    "사용자를 찾을 수 없습니다."
            );
        }

        // Quiz 조회
        Quiz quiz = quizRepository.findById(quizId).orElse(null);
        if (quiz == null) {
            return QuizValidationResultDto.invalid(
                    QuizSubmissionResultDto.SubmissionStatus.QUIZ_NOT_ACTIVE,
                    "존재하지 않는 퀴즈입니다."
            );
        }

        // QuizQuestion 조회
        QuizQuestionId questionKey = QuizQuestionId.of(questionId, quizId);
        QuizQuestion question = quizQuestionRepository.findById(questionKey).orElse(null);
        if (question == null) {
            return QuizValidationResultDto.invalid(
                    QuizSubmissionResultDto.SubmissionStatus.QUIZ_NOT_ACTIVE,
                    "존재하지 않는 문제입니다."
            );
        }

        // QuizOption 조회
        QuizOptionId qoId = QuizOptionId.of(optionId, questionId, quizId);
        QuizOption selectedOption = quizOptionRepository.findById(qoId).orElse(null);
        if (selectedOption == null) {
            return QuizValidationResultDto.invalid(
                    QuizSubmissionResultDto.SubmissionStatus.SYSTEM_ERROR,
                    "존재하지 않는 선택지입니다."
            );
        }

        // UserQuizAttempt 조회 또는 생성
        UserQuizAttempt attempt = userQuizAttemptRepository
                .findByQuizAndUser(quiz, user)
                .orElseGet(() -> createNewAttempt(quiz, user));

        return QuizValidationResultDto.valid(question, selectedOption, attempt, user);
    }

    private UserQuizAttempt createNewAttempt(Quiz quiz, User user) {
        UserQuizAttempt attempt = UserQuizAttempt.builder()
                .quiz(quiz)
                .user(user)
                .lastRound(1)
                .startedAt(LocalDateTime.now())
                .build();
        return userQuizAttemptRepository.save(attempt);
    }

    private UserQuizAnswer saveWrongAnswer(UserQuizAttempt attempt, QuizOption selectedOption) {
        UserQuizAnswer wrongAnswer = UserQuizAnswer.builder()
                .attempt(attempt)
                .quizOption(selectedOption)
                .answeredAt(LocalDateTime.now())
                .qualificationOrder(null)
                .advanced(false)
                .build();
        attempt.addAnswer(wrongAnswer);
        return userQuizAnswerRepository.save(wrongAnswer);
    }

    private UserQuizAnswer saveCorrectAnswer(UserQuizAttempt attempt, QuizOption selectedOption,
                                             Long qualificationOrder, boolean advanced) {
        UserQuizAnswer correctAnswer = UserQuizAnswer.builder()
                .attempt(attempt)
                .quizOption(selectedOption)
                .answeredAt(LocalDateTime.now())
                .qualificationOrder(qualificationOrder.intValue())
                .advanced(advanced)
                .build();
        attempt.addAnswer(correctAnswer);
        return userQuizAnswerRepository.save(correctAnswer);
    }

    /**
     * Redis Lua Script 생성 (선착순 처리용)
     */
    private RedisScript<Long> createSubmitAnswerScript() {
        String script = """
        -- Redis Keys
        local survivorKey = KEYS[1]     -- 생존자 랭킹 (Sorted Set)
        local countKey = KEYS[2]        -- 선착순 순위 카운터 (String)
        local progressKey = KEYS[3]     -- 문제 진행 정보 (Hash)
        
        -- Arguments
        local userId = ARGV[1]          -- 사용자 ID
        local submissionTime = tonumber(ARGV[2])  -- 제출 시간
        local firstCapacity = tonumber(ARGV[3])   -- 선착순 인원 제한
        
        -- 1. 중복 참여 확인
        if redis.call('ZSCORE', survivorKey, userId) then
            return -1 -- 이미 참여함
        end
        
        -- 2. 원자적으로 순위 할당
        local currentRank = redis.call('INCR', countKey)
        
        -- 3. 정원 내 여부 확인
        if currentRank <= firstCapacity then
            -- 생존자 목록에 추가
            redis.call('ZADD', survivorKey, submissionTime, userId)
            
            -- 진행 정보 업데이트
            redis.call('HSET', progressKey, 'lastUpdate', submissionTime)
            redis.call('HSET', progressKey, 'totalSubmissions', currentRank)
            redis.call('HSET', progressKey, 'survivors', redis.call('ZCARD', survivorKey))
            
            -- TTL 설정
            if currentRank == 1 then
                redis.call('EXPIRE', survivorKey, 7200)
                redis.call('EXPIRE', countKey, 7200)
                redis.call('EXPIRE', progressKey, 7200)
            end
            
            return currentRank
        else
            -- 정원 초과
            redis.call('HSET', progressKey, 'totalSubmissions', currentRank)
            return 0
        end
        """;

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(script);
        redisScript.setResultType(Long.class);
        return redisScript;
    }

    /**
     * Redis 키 관리
     */
    public static class RedisKeys {
        private static final String PREFIX = "quiz:question:";

        public static String survivorRanking(Long questionId) {
            return PREFIX + "survivors:" + questionId;
        }

        public static String submissionCount(Long questionId) {
            return PREFIX + "count:" + questionId;
        }

        public static String questionProgress(Long questionId) {
            return PREFIX + "progress:" + questionId;
        }
    }
}