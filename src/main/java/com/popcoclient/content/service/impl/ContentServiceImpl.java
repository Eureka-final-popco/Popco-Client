package com.popcoclient.content.service.impl;

import com.popcoclient.content.dto.response.DailyPopularContentResponseDto;
import com.popcoclient.content.repository.DailyPopularContentRepository;
import com.popcoclient.content.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContentServiceImpl implements ContentService {
    private final DailyPopularContentRepository dailyPopularContentRepository;

    @Override
    public List<DailyPopularContentResponseDto> getDailyPopularContent() {

        return List.of();
    }
}
