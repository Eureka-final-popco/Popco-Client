package com.popcoclient.content.service;

import com.popcoclient.content.dto.response.ContentDetailDto;
import com.popcoclient.content.dto.response.ContentListResponseDto_40;
import com.popcoclient.content.dto.response.ContentRecommendResponseDto;
import com.popcoclient.content.dto.response.DailyPopularContentResponseDto;

import java.util.List;

public interface ContentService {
    List<DailyPopularContentResponseDto> getDailyPopularContentList(String type);
    ContentDetailDto getContentDetail(Long id, String type);
    List<ContentRecommendResponseDto> getContentRecommendList(Long userId, String type);
    ContentListResponseDto_40 getContentPreferenceList(Long userId);
}
