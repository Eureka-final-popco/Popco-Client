package com.popcoclient.event.service;

import com.popcoclient.event.dto.response.QuizAlarmResponseDto;
import com.popcoclient.event.dto.response.QuizQuestionResponseDto;
import com.popcoclient.event.dto.response.QuizResponseDto;

public interface QuizService {
    // 페이지마다 내려줄 이벤트 시간
    QuizAlarmResponseDto getQuizAlarm();

    // 이벤트 페이지에 내려줄 정보
    QuizResponseDto getQuiz();

    QuizQuestionResponseDto getQuizQuestion(long userId, long quizId, long questionNum);
}
