package com.popcoclient.collection.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FailedContentDto {
    private Long contentId;
    private String contentType;
    private String reason;

    public static FailedContentDto of(Long contentId, String contentType, String reason) {
        return FailedContentDto.builder()
                .contentId(contentId)
                .contentType(contentType)
                .reason(reason)
                .build();
    }
}
