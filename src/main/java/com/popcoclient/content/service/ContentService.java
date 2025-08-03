package com.popcoclient.content.service;

import com.popcoclient.content.dto.response.*;
import com.popcoclient.content.entity.Content;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ContentService {
    Page<Content> getAllContents(Pageable pageable, String sortType);

    List<DailyPopularContentResponseDto> getDailyPopularContentList(Long userId, String type);

    ContentDetailDto getContentDetail(Long id, String type);

    List<ContentRecommendResponseDto> getContentRecommendList(Long userId, String type);
    ContentListResponseDto_40 getContentPreferenceList(Long userId);

    List<LikedContentResponseDto> getLikedContents(Long userId);
}
