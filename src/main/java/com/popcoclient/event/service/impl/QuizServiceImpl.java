package com.popcoclient.event.service.impl;

import com.popcoclient.event.dto.response.*;
import com.popcoclient.event.entity.Quiz;
import com.popcoclient.event.entity.QuizQuestion;
import com.popcoclient.event.repository.QuizOptionRepository;
import com.popcoclient.event.repository.QuizQuestionRepository;
import com.popcoclient.event.repository.QuizRepository;
import com.popcoclient.event.service.QuizService;
import com.popcoclient.exception.business.QuizMismatchForTodayException;
import com.popcoclient.exception.business.QuizNotFoundForTodayException;
import com.popcoclient.exception.business.UserNotFoundException;
import com.popcoclient.user.entity.User;
import com.popcoclient.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizOptionRepository quizOptionRepository;

    @Override
    public QuizAlarmResponseDto getQuizAlarm() {
        Optional<Quiz> optionalQuiz = getTodayQuiz();

        if (optionalQuiz.isEmpty()) {
            return QuizAlarmResponseDto.from(null,false,false);
        }

        Quiz quiz = optionalQuiz.get();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime quizStart = quiz.getStartAt();

        QuizAlarmDto quizAlarmDto = QuizAlarmDto.from(quiz,now);

        long minutesUntilStart = Duration.between(now, quizStart).toMinutes();
        boolean showPopup = minutesUntilStart <= 1 && minutesUntilStart > 0;

        if (showPopup) {
            return QuizAlarmResponseDto.from(quizAlarmDto,true,true);
        } else {
            return QuizAlarmResponseDto.from(quizAlarmDto,true,false);
        }
    }

    @Override
    public QuizResponseDto getQuiz() {
        Optional<Quiz> optionalQuiz = getTodayQuiz();

        if (optionalQuiz.isEmpty()) {
            return QuizResponseDto.from(null,false);
        }

        Quiz quiz = optionalQuiz.get();
        LocalDateTime now = LocalDateTime.now();
        QuizDetailDto detailDto = QuizDetailDto.from(quiz,now);

        return QuizResponseDto.from(detailDto, true);
    }

    @Override
    @Transactional
    public QuizQuestionResponseDto getQuizQuestion(Long userId, Long quizId, Integer questionOrder) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. userId: " + userId));

        Optional<Quiz> optionalQuiz = getTodayQuiz();

        if (optionalQuiz.isEmpty()) {
            throw new QuizNotFoundForTodayException();
        }

        Quiz quiz = optionalQuiz.get();
        LocalDateTime now = LocalDateTime.now();

        if (quiz.getQuizId() != quizId) {
            throw new QuizMismatchForTodayException();
        }

        if(quiz.getRoundCount() < questionOrder) {
            throw new IllegalArgumentException("해당 질문은 존재하지 않습니다.");
        }

        boolean lastRound = quiz.getRoundCount() == questionOrder;

        QuizQuestion question = quizQuestionRepository.findByQuizAndQuestionOrder(quiz, questionOrder);
        List<QuizOptionDto> options =
                quizOptionRepository.findByQuizIdAndQuestionId(quiz.getQuizId(), question.getQuestionId().getQuestionId());

        return QuizQuestionResponseDto.from(question, options, now, lastRound);
    }

    private Optional<Quiz> getTodayQuiz() {
        LocalDate today = LocalDate.now(); // 테스트 용이성 ↑
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        return quizRepository.findFirstByStartAtBetween(start, end);
    }


}
