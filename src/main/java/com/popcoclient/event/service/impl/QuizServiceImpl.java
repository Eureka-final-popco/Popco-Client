package com.popcoclient.event.service.impl;

import com.popcoclient.event.dto.response.QuizAlarmResponseDto;
import com.popcoclient.event.dto.response.QuizDetailDto;
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
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        Optional<Quiz> optionalQuiz = quizRepository.findFirstByStartAtBetween(
                today.atStartOfDay(),
                today.plusDays(1).atStartOfDay()
        );

        if (optionalQuiz.isEmpty()) {
            return null;
        }

        return QuizAlarmResponseDto.from(optionalQuiz.get(),now);
    }

    @Override
    public QuizResponseDto getQuiz() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        Optional<Quiz> optionalQuiz = quizRepository.findFirstByStartAtBetween(
                today.atStartOfDay(),
                today.plusDays(1).atStartOfDay()
        );

        if (optionalQuiz.isEmpty()) {
            return QuizResponseDto.from(null,false);
        }

        Quiz quiz = optionalQuiz.get();
        QuizDetailDto detailDto = QuizDetailDto.from(quiz,now);

        return QuizResponseDto.from(detailDto, true);
    }

}
