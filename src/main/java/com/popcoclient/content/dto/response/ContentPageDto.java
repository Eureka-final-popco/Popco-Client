package com.popcoclient.content.dto.response;

import com.popcoclient.content.entity.Content;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class ContentPageDto {

    private List<ContentDto> contents;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
    private boolean first;
    private boolean last;

    public ContentPageDto(Page<Content> contentPage) {
        this.contents = contentPage.getContent().stream()
                .map(ContentDto::from)
                .collect(Collectors.toList());

        this.totalElements = contentPage.getTotalElements();
        this.totalPages = contentPage.getTotalPages();
        this.currentPage = contentPage.getNumber();
        this.pageSize = contentPage.getSize();
        this.first = contentPage.isFirst();
        this.last = contentPage.isLast();
    }
}