package com.popcoclient.content.service;

import com.popcoclient.content.dto.response.DailyPopularContentResponseDto;

import java.util.List;

public interface ContentService {
    List<DailyPopularContentResponseDto> getDailyPopularContent(String type);
}
