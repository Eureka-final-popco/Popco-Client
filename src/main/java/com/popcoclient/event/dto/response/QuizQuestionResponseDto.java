package com.popcoclient.event.dto.response;

import com.popcoclient.event.entity.QuizOption;
import com.popcoclient.event.entity.QuizQuestion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuizQuestionResponseDto {
    private Long quizId;
    private Long questionId;
    private String content;
    private Long quizQuestionId;
    private Integer quizOrder;
    private String quizContent;
    private String imgPath;
    private LocalDateTime finishedAt;
    private LocalDateTime serverTime;
    private Integer firstCapacity;
    private List<QuizQuestionsOptionsResponseDto> options;
    private List<QuizOptionDto> optionList;
    private boolean isLastRound;

    public static QuizQuestionResponseDto from(QuizQuestion quizQuestion, List<QuizOptionDto> optionList, LocalDateTime serverTime, boolean lastRound) {
        return QuizQuestionResponseDto.builder()
                .quizId(quizQuestion.getQuestionId().getQuizId())
                .quizQuestionId(quizQuestion.getQuestionId().getQuestionId())
                .quizOrder(quizQuestion.getQuestionOrder())
                .quizContent(quizQuestion.getContent())
                .imgPath(quizQuestion.getImgPath())
                .finishedAt(quizQuestion.getFinishedAt())
                .serverTime(serverTime)
                .firstCapacity(quizQuestion.getFirstCapacity())
                .optionList(optionList)
                .isLastRound(lastRound)
                .build();
    }
}
