package com.popcoclient.content.service;

import com.popcoclient.content.dto.response.ContentDetailDto;
import com.popcoclient.content.dto.response.DailyPopularContentResponseDto;
import com.popcoclient.content.entity.Content;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ContentService {
    Page<Content> getAllContents(Pageable pageable);

    List<DailyPopularContentResponseDto> getDailyPopularContent(String type);

    ContentDetailDto getContentDetail(Long id, String type);
}
