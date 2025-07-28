package com.popcoclient.content.dto.response;

import com.popcoclient.content.entity.enums.ReactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContentRecommendResponseDto {
    private Long contentId;
    private String contentType;
    private String title;
    private Integer rank;
    private String posterPath;
    private ReactionType reactionType;
}
