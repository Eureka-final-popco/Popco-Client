package com.popcoclient.content.service;

import com.popcoclient.content.dto.response.*;
import com.popcoclient.content.entity.Content;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ContentService {
    ContentPageDto getAllContents(Pageable pageable, String sortType, Long userId);

    List<DailyPopularContentResponseDto> getDailyPopularContentList(Long userId, String type);

    ContentDetailDto getContentDetail(Long id, String type, Long userId);

    List<ContentRecommendResponseDto> getContentRecommendList(Long userId, String type);
    ContentListResponseDto_40 getContentPreferenceList(Long userId);

    List<LikedContentResponseDto> getLikedContents(Long userId);
}
