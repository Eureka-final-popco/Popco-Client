package com.popcoclient.content.dto.response;

import com.popcoclient.content.document.ContentFilterDocument;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedContentFilterResponseDto {
    private List<ContentFilterDocument> contents;
    private long totalElements;
    private int page;
    private int size;
    private int totalPages;

    public PaginatedContentFilterResponseDto(List<ContentFilterDocument> contents, long totalElements, int page, int size) {
        this.contents = contents;
        this.totalElements = totalElements;
        this.page = page;
        this.size = size;
        this.totalPages = (int) Math.ceil((double) totalElements / size);
    }
}