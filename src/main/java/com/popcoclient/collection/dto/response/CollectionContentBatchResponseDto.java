package com.popcoclient.collection.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionContentBatchResponseDto {

    @Builder.Default
    private List<CollectionContentResponseDto> successContents = new ArrayList<>();

    @Builder.Default
    private List<FailedContentDto> failedContents = new ArrayList<>();

    private int totalRequested;
    private int successCount;
    private int failedCount;

    public static CollectionContentBatchResponseDto of(List<CollectionContentResponseDto> successContents,
                                                       List<FailedContentDto> failedContents,
                                                       int totalRequested) {
        return CollectionContentBatchResponseDto.builder()
                .successContents(successContents)
                .failedContents(failedContents)
                .totalRequested(totalRequested)
                .successCount(successContents.size())
                .failedCount(failedContents.size())
                .build();
    }
}
