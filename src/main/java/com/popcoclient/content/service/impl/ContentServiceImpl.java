package com.popcoclient.content.service.impl;

import com.popcoclient.content.dto.response.DailyPopularContentResponseDto;
import com.popcoclient.content.entity.Content;
import com.popcoclient.content.entity.DailyPopularContent;
import com.popcoclient.content.entity.Genre;
import com.popcoclient.content.entity.key.ContentId;
import com.popcoclient.content.repository.DailyPopularContentRepository;
import com.popcoclient.content.repository.GenreRepository;
import com.popcoclient.content.service.ContentService;
import com.popcoclient.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContentServiceImpl implements ContentService {
    private final DailyPopularContentRepository dailyPopularContentRepository;
    private final GenreRepository genreRepository;
    private final ReviewRepository reviewRepository;

    public List<DailyPopularContentResponseDto> getDailyPopularContent(String type) {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String batchType = type == null ? null : type.trim().toUpperCase();

        List<DailyPopularContent> popularContentList =
                dailyPopularContentRepository.findByBatchContentTypeAndRankedDate(batchType, yesterday);

        if (popularContentList.isEmpty()) {
            return Collections.emptyList();
        }

        // 모든 장르 ID를 미리 조회해서 매핑 생성
        Set<Integer> allGenreIds = popularContentList.stream()
                .flatMap(pc -> pc.getContent().getGenreIds().stream())
                .collect(Collectors.toSet());

        Map<Integer, String> genreMap = genreRepository.findAllById(allGenreIds).stream()
                .collect(Collectors.toMap(Genre::getId, Genre::getName));

        Map<ContentId, Double> reviewAvgMap = reviewRepository.findAverageScoreByContents(
                popularContentList.stream()
                        .map(DailyPopularContent::getContent)
                        .map(Content::getContentId)
                        .collect(Collectors.toSet())
        );

        // DTO 변환
        return popularContentList.stream()
                .map(pc -> mapToDto(pc, genreMap, reviewAvgMap))
                .toList();
    }

    private DailyPopularContentResponseDto mapToDto(
            DailyPopularContent pc, Map<Integer, String> genreMap, Map<ContentId, Double> reviewAvgMap) {
        Content content = pc.getContent();
        ContentId contentId = content.getContentId();

        // 장르 이름 합치기
        String genres = content.getGenreIds().stream()
                .map(genreMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(","));

        // 리뷰 평점 평균 가져오기
        Double reviewAvg = reviewAvgMap.getOrDefault(contentId, null);

        return DailyPopularContentResponseDto.of(pc, genres, reviewAvg);
    }
}
