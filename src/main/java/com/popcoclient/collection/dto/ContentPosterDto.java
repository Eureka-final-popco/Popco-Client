package com.popcoclient.collection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentPosterDto {
    private Long contentId;
    private String contentType;
    private String posterPath;
    private String title;
}
