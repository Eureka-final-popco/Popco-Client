package com.popcoclient.event.service.impl;

import com.popcoclient.event.dto.request.QuizSubmissionResultDto;
import com.popcoclient.event.dto.response.*;
import com.popcoclient.event.entity.*;
import com.popcoclient.event.entity.enums.QuizStatus;
import com.popcoclient.event.entity.key.QuizOptionId;
import com.popcoclient.event.entity.key.QuizQuestionId;
import com.popcoclient.event.repository.*;
import com.popcoclient.exception.BusinessException;
import com.popcoclient.exception.ErrorCode;
import com.popcoclient.user.entity.User;
import com.popcoclient.user.entity.UserDetail;
import com.popcoclient.user.repository.UserDetailRepository;
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
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
@Transactional
@Slf4j
public class EventServiceImpl {

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

    private final Map<String, Long> activeTimers = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> activeBroadcasts = new ConcurrentHashMap<>();
    private final Map<Long, ScheduledFuture<?>> waitingTimerBroadcasts = new ConcurrentHashMap<>();
    private final UserDetailRepository userDetailRepository;

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
            TaskScheduler taskScheduler,
            UserDetailRepository userDetailRepository) {
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
        this.userDetailRepository = userDetailRepository;
    }

    public Long getQuizId(){
     Long latest = quizRepository.findMaxId();
     return latest;
    }

    public CurrentQuestionResponseDto getQuizQuestion(Long quizId, Long questionId){
        QuizQuestionId quizQuestionId = QuizQuestionId.of(questionId, quizId);
        QuizQuestion quizQuestion = quizQuestionRepository.findById(quizQuestionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUESTION_NOT_FOUND));
        List<QuizOption> quizOption = quizQuestion.getOptions();
        List<QuizQuestionsOptionsResponseDto> optionsList = new ArrayList<>();

        for (QuizOption quizOptionDto : quizOption) {
            QuizQuestionsOptionsResponseDto optionsDto = new QuizQuestionsOptionsResponseDto(quizOptionDto.getContent(), quizOptionDto.getIsCorrect());
            optionsList.add(optionsDto);
        }

        return CurrentQuestionResponseDto.builder()
                .quizId(quizId)
                .questionId(questionId)
                .content(quizQuestion.getContent())
                .firstCapacity(quizQuestion.getFirstCapacity())
                .options(optionsList)
                .build();
    }

    /**
     *     답안 제출
      */
    public QuizSubmissionResultDto submitAnswer(Long quizId, Long questionId, Long userId, Long optionId) {
        long startTime = System.currentTimeMillis();
        log.info("Answer submission started - quizId: {}, questionId: {}, userId: {}, optionId: {}",
                quizId, questionId, userId, optionId);

        QuizValidationResultDto validation = validateAndLoadEntities(quizId, questionId, userId, optionId);
        if (!validation.isValid()) {
            return createErrorResult(validation.getErrorStatus(), validation.getErrorMessage());
        }

        QuizQuestion question = validation.getQuestion();
        QuizOption selectedOption = validation.getSelectedOption();
        UserQuizAttempt attempt = validation.getAttempt();

        // 중복 제출 방지 락
        String duplicateLockKey = DistributeLockServiceImpl.LockKeys.participantAnswer(questionId, userId);
        String lockValue = UUID.randomUUID().toString();

        if (!lockService.tryLock(duplicateLockKey, lockValue, 30)) {
            log.warn("Duplicate submission attempt - questionId: {}, userId: {}", questionId, userId);
            return QuizSubmissionResultDto.duplicate();
        }

        try {
            QuizQuestionId questionKey = QuizQuestionId.of(questionId, quizId);
            boolean alreadyAnswered = userQuizAnswerRepository.existsByAttemptAndQuestionId(attempt, questionKey);
            if (alreadyAnswered) {
                return QuizSubmissionResultDto.alreadySubmitted();
            }

            if (!selectedOption.getIsCorrect()) {
                saveWrongAnswer(attempt, selectedOption);
                return QuizSubmissionResultDto.wrongAnswer();
            }

            QuizSubmissionResultDto result = processCorrectAnswer(question, selectedOption, attempt, startTime);

            return result;

        } finally {
            lockService.unlock(duplicateLockKey, lockValue);
        }
    }

    /**
     *     퀴즈 상태 조회
     */
    public QuizStatusResponseDto getQuizStatus(Long quizId, Long questionId) {
        try {
            QuizQuestionId questionKey = QuizQuestionId.of(questionId, quizId);
            QuizQuestion question = quizQuestionRepository.findById(questionKey)
                    .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));

            int currentSurvivors = getTotalSurvivors(questionId);
            int maxSurvivors = question.getFirstCapacity();

            String timerKey = quizId + ":" + questionId;
            Long startTime = activeTimers.get(timerKey);

            int remainingTime = 0;
            QuizStatus status = QuizStatus.fromTimerStatus(startTime);
            boolean isActive = (status == QuizStatus.ACTIVE);

            if (startTime != null) {
                long elapsed = System.currentTimeMillis() - startTime;
                remainingTime = Math.max(0, 10 - (int) (elapsed / 1000));
            }

            return QuizStatusResponseDto.builder()
                    .quizId(quizId)
                    .questionId(questionId)
                    .currentSurvivors(currentSurvivors)
                    .maxSurvivors(maxSurvivors)
                    .isActive(isActive)
                    .remainingTime(remainingTime)
                    .status(status)
                    .build();

        } catch (Exception e) {
            log.error("Failed to get quiz status - quizId: {}, questionId: {}", quizId, questionId, e);
            throw new RuntimeException("퀴즈 상태 조회에 실패했습니다.", e);
        }
    }

    /**
     *     실시간 브로드캐스트
     */
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

    public void startTimer(Long quizId) {
        String timerKey = "quiz" + quizId + "timer";

        activeTimers.computeIfAbsent(timerKey, key -> {
            long startTime = System.currentTimeMillis();

            taskScheduler.scheduleAtFixedRate(() -> {
                long elapsed = (System.currentTimeMillis() - startTime) / 1000;
                int remainTime = 30 - (int)elapsed;

                if (remainTime >= 0) {
                    sendTimer(quizId, remainTime);
                } else {
                    sendTimer(quizId, 0);
                    activeTimers.remove(timerKey);
                }
            }, Instant.now().plus(1, ChronoUnit.SECONDS), Duration.ofSeconds(1));

            return startTime;
        });
    }

    private void sendTimer(Long quizId, int remainTime) {
        String questionTopic = "/topic/quiz/" + quizId + "/timer";
        Map<String, Object> timer = Map.of(
                "type", "Timer",
                "quizId", quizId,
                "remain", remainTime
        );
        messagingTemplate.convertAndSend(questionTopic, timer);
    }

    /**
     *     생존자 순위 조회
     */
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

    /**
     * 다음 문제 준비 체크 및 시작
     */
    private void checkAndStartNextQuestion(Long quizId, Long questionId) {

        QuizQuestionId currentQuestionKey = QuizQuestionId.of(questionId, quizId);
        QuizQuestion currentQuestion = quizQuestionRepository.findById(currentQuestionKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));

        int currentSurvivors = getTotalSurvivors(questionId);
        int currentCapacity = currentQuestion.getFirstCapacity();

        Long nextQuestionId = questionId + 1;

        QuizQuestionId nextQuestionKey = QuizQuestionId.of(nextQuestionId, quizId);
        if (!quizQuestionRepository.existsById(nextQuestionKey)) {
            log.info("마지막 문제 완료! 퀴즈 종료");

            String currentTimerKey = quizId + ":" + questionId;
            activeTimers.remove(currentTimerKey);
            return;
        }

        String timerKey = quizId + ":" + nextQuestionId;

        // 이미 시작된 문제는 중복 시작 방지
        if (!activeTimers.containsKey(timerKey)) {
            log.info("다음 문제 {} 시작", nextQuestionId);

            // 이전 라운드 타임 아웃 즉시 다음 문제 시작
            taskScheduler.schedule(() -> {
                startQuestion(quizId, nextQuestionId);
            }, Instant.now());
        } else {
            log.warn("문제 {} 이미 시작됨 - 중복 방지", nextQuestionId);
        }
    }

    /**
     * 문제 시작 이전 라운드 타임 아웃 시 즉시 시작
     */
    private void startQuestion(Long quizId, Long questionId) {
        String timerKey = quizId + ":" + questionId;

        activeTimers.computeIfAbsent(timerKey, k -> {
            long questionStartTime = System.currentTimeMillis();
            log.info("문제 {}를 새로 시작하고 타이머를 등록합니다.", questionId);

            ScheduledFuture<?> timerBroadcast = taskScheduler.scheduleAtFixedRate(() -> {
                try {
                    QuizStatusResponseDto currentStatus = getQuizStatus(quizId, questionId);
                    String questionTopic = "/topic/quiz/" + quizId + "/question/" + questionId;
                    messagingTemplate.convertAndSend(questionTopic, currentStatus);
                } catch (Exception e) {
                    log.error("Failed to broadcast timer for question: {}", questionId, e);
                }
            }, Instant.now().plus(1, ChronoUnit.SECONDS), Duration.ofSeconds(1));

            activeBroadcasts.put(timerKey, timerBroadcast);

            taskScheduler.schedule(() -> {
                activeTimers.remove(timerKey);

                ScheduledFuture<?> broadcast = activeBroadcasts.remove(timerKey);
                if (broadcast != null && !broadcast.isCancelled()) {
                    broadcast.cancel(false);
                }

                broadcastQuestionTimeout(quizId, questionId);
                checkAndStartNextQuestionAfterTimeout(quizId, questionId);
            }, Instant.now().plus(10, ChronoUnit.SECONDS));

            broadcastQuestionStart(quizId, questionId);

            return questionStartTime;
        });
    }

    /**
     * 문제1 시작 (퀴즈 시작 버튼으로 즉시 시작)
     */
    public void startFirstQuestion(Long quizId) {
        Long questionId = 1L;
        String timerKey = quizId + ":" + questionId;

        if (activeTimers.containsKey(timerKey)) {
            log.warn("문제1 이미 시작됨 - quizId: {}", quizId);
            return;
        }

        log.info("문제1 시작! 즉시 타이머 가동 - quizId: {}", quizId);

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
                    "message", "문제가 시작되었습니다! 10초의 시간이 주어집니다."
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
     * 10초 타임아웃 후 다음 문제 시작 체크
     */
    private void checkAndStartNextQuestionAfterTimeout(Long quizId, Long questionId) {
        try {
            Long nextQuestionId = questionId + 1;

            QuizQuestionId nextQuestionKey = QuizQuestionId.of(nextQuestionId, quizId);
            if (!quizQuestionRepository.existsById(nextQuestionKey)) {
                int currentSurvivors = getTotalSurvivors(questionId);
                if (currentSurvivors > 0) {
                    Long winnerId = getFirstPlaceSurvivor(questionId);
                    if (winnerId != null) {
                        announceWinner(quizId, questionId, winnerId, 1);
                    }
                } else {
                    // 아무도 정답 못함
                    announceNoWinner(quizId, questionId);
                }
                return;
            }

            int currentSurvivors = getTotalSurvivors(questionId);
            if (currentSurvivors > 0) {

                String timerKey = quizId + ":" + nextQuestionId;
                if (!activeTimers.containsKey(timerKey)) {
                    taskScheduler.schedule(() -> {
                        startQuestion(quizId, nextQuestionId);
                    }, Instant.now());
                }
            } else {
                log.info("생존자가 없어서 퀴즈 종료");
            }

        } catch (Exception e) {
            log.error("타임아웃 후 다음 문제 시작 체크 실패", e);
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
        QuizOptionId.of(selectedOption.getOptionId().getOptionId(), selectedOption.getOptionId().getQuestionId(), selectedOption.getOptionId().getQuizId());

        int firstCapacity = question.getFirstCapacity();
        long submissionTime = System.currentTimeMillis();

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

        boolean advanced = (qualificationOrder > 0 && qualificationOrder <= firstCapacity);
        saveCorrectAnswer(attempt, selectedOption, qualificationOrder, advanced);

        if (advanced) {
            if (isLastQuestion(quizId, questionId) && qualificationOrder == 1) { // 우승자 결정 로직
                announceWinner(quizId, questionId, userId, 1);
                UserDetail userDetail = userDetailRepository.findById(userId).orElseThrow(()->new BusinessException(ErrorCode.USER_NOT_FOUND));
                return QuizSubmissionResultDto.survived(qualificationOrder.intValue(), 1, userDetail.getNickname());
            }
            int totalSurvivors = getTotalSurvivors(questionId);
            broadcastSurvivorUpdate(quizId, questionId, qualificationOrder.intValue(), totalSurvivors);
            UserDetail userDetail = userDetailRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            return QuizSubmissionResultDto.survived(qualificationOrder.intValue(), totalSurvivors, userDetail.getNickname());
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

    /**
     * 이벤트 시작까지 남은 시간 조회
     */
    public QuizWaitingResponseDto getEventTimer(Quiz quiz) {
        try {
            LocalDateTime currentTime = LocalDateTime.now();
            LocalDateTime eventStartTime = quiz.getStartAt();

            Duration duration = Duration.between(currentTime, eventStartTime);
            long remainingSeconds = Math.max(0, duration.getSeconds());

            boolean isEventStarted = currentTime.isAfter(eventStartTime) || currentTime.isEqual(eventStartTime);
            QuizStatus quizStatus;

            if (isEventStarted) {
                String firstQuestionTimerKey = quiz.getQuizId() + ":" + 1L;
                if (activeTimers.containsKey(firstQuestionTimerKey)) {
                    quizStatus = QuizStatus.ACTIVE;
                } else {
                    quizStatus = QuizStatus.FINISHED;
                }
            } else {
                quizStatus = QuizStatus.WAITING;
            }

            List<Long> formattedTime = formatRemainingTime(remainingSeconds);

            return QuizWaitingResponseDto.builder()
                    .quizId(quiz.getQuizId())
                    .remainingHour(formattedTime.get(0))
                    .remainingMin(formattedTime.get(1))
                    .remainingSec(formattedTime.get(2))
                    .quizStatus(quizStatus)
                    .remainingTime(remainingSeconds)
                    .build();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to get event timer - quizId: {}", quiz.getQuizId(), e);
            throw new RuntimeException("이벤트 타이머 조회에 실패했습니다.", e);
        }
    }

    /**
     * 이벤트 대기 타이머 브로드캐스트 시작
     */
    public void startEventWaitingBroadcast(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));

        LocalDateTime eventStartTime = quiz.getStartAt();
        LocalDateTime currentTime = LocalDateTime.now();
        if (currentTime.isAfter(eventStartTime) || currentTime.isEqual(eventStartTime)) {
            log.info("이미 시작되었거나 종료된 이벤트입니다. 대기 타이머를 시작하지 않습니다. - quizId: {}", quizId);
            return;
        }

        waitingTimerBroadcasts.computeIfAbsent(quizId, id -> {

            return taskScheduler.scheduleAtFixedRate(() -> {
                try {
                    QuizWaitingResponseDto timerInfo = getEventTimer(quiz);
                    String waitingTopic = "/topic/quiz/" + id + "/waiting";
                    messagingTemplate.convertAndSend(waitingTopic, timerInfo);

                    if (timerInfo.getQuizStatus() != QuizStatus.WAITING) {
                        stopEventWaitingBroadcast(id);
                    }
                } catch (Exception e) {
                    log.error("대기 타이머 브로드캐스트 실행 중 예외 발생 - quizId: {}", id, e);
                }
            }, Instant.now(), Duration.ofSeconds(1));
        });
    }

    /**
     * 이벤트 대기 타이머 브로드캐스트 중단
     */
    public void stopEventWaitingBroadcast(Long quizId) {
        ScheduledFuture<?> broadcast = waitingTimerBroadcasts.remove(quizId);
        if (broadcast != null && !broadcast.isCancelled()) {
            broadcast.cancel(false);
        }
    }

    /**
     * 우승자 결정 및 브로드캐스트
     */
    private void announceWinner(Long quizId, Long questionId, Long winnerId, int winnerRank) {
        try {
            UserDetail winner = userDetailRepository.findById(winnerId)
                    .orElse(null);

            String winnerName = winner != null ? winner.getNickname() : "익명";

            String questionTopic = "/topic/quiz/" + quizId + "/question/" + questionId;
            Map<String, Object> winnerMessage = Map.of(
                    "type", "WINNER_ANNOUNCED",
                    "winnerName", winnerName,
                    "winnerRank", winnerRank,
                    "message", String.format("축하합니다! %s님이 우승하셨습니다!", winnerName)
            );

            messagingTemplate.convertAndSend(questionTopic, winnerMessage);
            log.info("우승자 발표 완료 - quizId: {}, winnerId: {}, winnerName: {}",
                    quizId, winnerId, winnerName);

        } catch (Exception e) {
            log.error("우승자 발표 실패 - quizId: {}, questionId: {}", quizId, questionId, e);
        }
    }

    private void announceNoWinner(Long quizId, Long questionId) {
        try {
            String questionTopic = "/topic/quiz/" + quizId + "/question/" + questionId;
            Map<String, Object> noWinnerMessage = Map.of(
                    "type", "NO_WINNER",
                    "quizId", quizId,
                    "questionId", questionId,
                    "message", "아쉽게도 우승자가 없습니다. 다음 기회에 도전해보세요!"
            );

            messagingTemplate.convertAndSend(questionTopic, noWinnerMessage);

        } catch (Exception e) {
            log.error("우승자 없음 발표 실패 - quizId: {}, questionId: {}", quizId, questionId, e);
        }
    }

    /**
     * Redis에서 1등 생존자 조회
     */
    private Long getFirstPlaceSurvivor(Long questionId) {
        try {
            String survivorKey = RedisKeys.survivorRanking(questionId);
            Set<String> firstPlace = eventRedisTemplate.opsForZSet().range(survivorKey, 0, 0);

            if (firstPlace != null && !firstPlace.isEmpty()) {
                String userIdStr = firstPlace.iterator().next();
                return Long.parseLong(userIdStr);
            }

            return null;
        } catch (Exception e) {
            log.error("1등 생존자 조회 실패 - questionId: {}", questionId, e);
            return null;
        }
    }

    /**
     * 마지막 문제인지 확인
     */
    private boolean isLastQuestion(Long quizId, Long questionId) {
        try {
            Long nextQuestionId = questionId + 1;
            QuizQuestionId nextQuestionKey = QuizQuestionId.of(nextQuestionId, quizId);
            return !quizQuestionRepository.existsById(nextQuestionKey);
        } catch (Exception e) {
            log.error("마지막 문제 확인 실패 - quizId: {}, questionId: {}", quizId, questionId, e);
            return false;
        }
    }

    private QuizSubmissionResultDto createErrorResult(QuizSubmissionResultDto.SubmissionStatus status, String message) {
        return QuizSubmissionResultDto.builder()
                .status(status)
                .message(message)
                .survived(false)
                .submissionTime(System.currentTimeMillis())
                .build();
    }

    private QuizValidationResultDto validateAndLoadEntities(Long quizId, Long questionId, Long userId, Long optionId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return QuizValidationResultDto.invalid(
                    QuizSubmissionResultDto.SubmissionStatus.SYSTEM_ERROR,
                    "사용자를 찾을 수 없습니다."
            );
        }

        Quiz quiz = quizRepository.findById(quizId).orElse(null);
        if (quiz == null) {
            return QuizValidationResultDto.invalid(
                    QuizSubmissionResultDto.SubmissionStatus.QUIZ_NOT_ACTIVE,
                    "존재하지 않는 퀴즈입니다."
            );
        }

        QuizQuestionId questionKey = QuizQuestionId.of(questionId, quizId);
        QuizQuestion question = quizQuestionRepository.findById(questionKey).orElse(null);
        if (question == null) {
            return QuizValidationResultDto.invalid(
                    QuizSubmissionResultDto.SubmissionStatus.QUIZ_NOT_ACTIVE,
                    "존재하지 않는 문제입니다."
            );
        }

        QuizOptionId qoId = QuizOptionId.of(optionId, questionId, quizId);
        QuizOption selectedOption = quizOptionRepository.findById(qoId).orElse(null);
        if (selectedOption == null) {
            return QuizValidationResultDto.invalid(
                    QuizSubmissionResultDto.SubmissionStatus.SYSTEM_ERROR,
                    "존재하지 않는 선택지입니다."
            );
        }

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
     * 남은 시간 포맷
     */
    private List<Long> formatRemainingTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        List<Long> timers = new ArrayList<>();
        timers.add(hours);
        timers.add(minutes);
        timers.add(secs);
        return timers;
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
                
                -- 중복 참여 확인
                if redis.call('ZSCORE', survivorKey, userId) then
                    return -1
                end
                
                -- 원자적으로 순위 할당
                local currentRank = redis.call('INCR', countKey)
                
                -- 정원 내 여부 확인
                if currentRank <= firstCapacity then
                    -- 생존자 목록에 추가
                    redis.call('ZADD', survivorKey, submissionTime, userId)
                
                    -- 진행 정보 업데이트
                    redis.call('HSET', progressKey, 'lastUpdate', submissionTime)
                    redis.call('HSET', progressKey, 'totalSubmissions', currentRank)
                    redis.call('HSET', progressKey, 'survivors', redis.call('ZCARD', survivorKey))
                
                    if currentRank == 1 then
                        redis.call('EXPIRE', survivorKey, 30)
                        redis.call('EXPIRE', countKey, 30)
                        redis.call('EXPIRE', progressKey, 30)
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