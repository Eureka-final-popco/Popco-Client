package com.popcoclient.content.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContentSimpleResponseDto {
    private Long id;
    private String type;
    private String title;
    private String posterPath;
}
