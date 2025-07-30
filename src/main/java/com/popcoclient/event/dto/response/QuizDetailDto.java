package com.popcoclient.event.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuizDetailDto {
    private String eventId;
    private String eventName;
    private String eventContentTitle;
    private String eventContentPosterUrl;
    private String eventReward;
    private LocalDateTime eventStartTime;
    private LocalDateTime serverTime;
}
