package com.popcoclient.event.service.impl;

import com.popcoclient.event.dto.response.QuizAlarmResponseDto;
import com.popcoclient.event.dto.response.QuizResponseDto;
import com.popcoclient.event.entity.Quiz;
import com.popcoclient.event.repository.QuizRepository;
import com.popcoclient.event.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;

    @Override
    public QuizAlarmResponseDto getQuizAlarm(Long userId) {
        if(userId == null) {
            throw new RuntimeException("퀴즈 알람은 로그인 사용자에게만 적용됩니다.");
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        // 오늘 날짜의 퀴즈 중 하나만 조회 (시작 시간이 미래인 것만)
        Optional<Quiz> optionalQuiz = quizRepository.findFirstByStartAtBetween(
                today.atStartOfDay(),
                today.plusDays(1).atStartOfDay()
        );

        if (optionalQuiz.isEmpty()) {
            return QuizAlarmResponseDto.from(null,false);
        }

        Quiz quiz = optionalQuiz.get();
        LocalDateTime quizStart = quiz.getStartAt();

        long minutesUntilStart = Duration.between(now, quizStart).toMinutes();
        boolean showPopup = minutesUntilStart <= 10 && minutesUntilStart > 0;

        return QuizAlarmResponseDto.from(quiz,showPopup);
    }

    @Override
    public QuizResponseDto getQuiz() {
        return null;
    }
}
