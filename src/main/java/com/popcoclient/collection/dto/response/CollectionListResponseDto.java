package com.popcoclient.collection.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionListResponseDto {

    private List<CollectionResponseDto> collections;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private boolean hasNext;

    public static CollectionListResponseDto from(Page<CollectionResponseDto> page) {
        return CollectionListResponseDto.builder()
                .collections(page.getContent())
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .hasNext(page.hasNext())
                .build();
    }
}
