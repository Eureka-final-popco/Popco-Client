package com.popcoclient.content.service.impl;

import com.popcoclient.content.dto.response.*;
import com.popcoclient.content.entity.*;
import com.popcoclient.content.entity.enums.ReactionType;
import com.popcoclient.content.entity.key.ContentId;
import com.popcoclient.content.repository.*;
import com.popcoclient.content.service.ContentService;
import com.popcoclient.exception.BusinessException;
import com.popcoclient.exception.ErrorCode;
import com.popcoclient.exception.business.UserNotFoundException;
import com.popcoclient.user.entity.User;
import com.popcoclient.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContentServiceImpl implements ContentService {
    private final DailyPopularContentRepository dailyPopularContentRepository;
    private final GenreRepository genreRepository;
    private final ContentRepository contentRepository;
    private final ContentReactionRepository contentReactionRepository;
    private final UserRepository userRepository;
    private final ContentRecommendationRepository contentRecommendationRepository;

    public Page<Content> getAllContents(Pageable pageable, String sortType) {
        if (sortType == null) {
            sortType = "recent";
        }

        if ("recent".equalsIgnoreCase(sortType)) {
            return contentRepository.findAll(pageable);
        } else if ("popular".equalsIgnoreCase(sortType)) {
            return contentRepository.findAllOrderByPopularity(pageable);
        } else {
            throw new IllegalArgumentException("지원하지 않는 정렬 타입입니다: " + sortType);
        }
    }

    @Override
    public List<DailyPopularContentResponseDto> getDailyPopularContentList(Long userId, String type) {
        List<DailyPopularContent> contents = loadPopularContents(type);
        Map<Integer, String> genreMap = loadGenreMap(contents);

        Map<ContentId, ReactionType> userReactions = (userId == null)
                ? Collections.emptyMap()
                : loadUserReactions(userId, contents);

        return contents.stream()
                .map(pc -> mapToDto(pc, genreMap, userReactions.get(pc.getContent().getContentId())))
                .toList();
    }

    private List<DailyPopularContent> loadPopularContents(String type) {
        String batchType = (type == null) ? null : type.trim().toUpperCase();
        LocalDate today = LocalDate.now();

        List<DailyPopularContent> contents =
                dailyPopularContentRepository.findByBatchContentTypeAndRankedDate(batchType, today);

        if (contents.isEmpty()) {
            LocalDate yesterday = today.minusDays(1);
            contents = dailyPopularContentRepository.findByBatchContentTypeAndRankedDate(batchType, yesterday);
        }

        return contents;
    }

    private Map<Integer, String> loadGenreMap(List<DailyPopularContent> contents) {
        Set<Integer> genreIds = contents.stream()
                .flatMap(pc -> pc.getContent().getGenreIds().stream())
                .collect(Collectors.toSet());

        return genreRepository.findAllById(genreIds).stream()
                .collect(Collectors.toMap(Genre::getId, Genre::getName));
    }

    private Map<ContentId, ReactionType> loadUserReactions(Long userId, List<DailyPopularContent> contents) {
        Set<ContentId> contentIds = contents.stream()
                .map(pc -> pc.getContent().getContentId())
                .collect(Collectors.toSet());

        System.out.println(contentIds.contains(new ContentId(254017L, "tv")));

        return contentReactionRepository.findByUserIdAndContentIds(userId, contentIds).stream()
                .collect(Collectors.toMap(
                        cr -> cr.getContent().getContentId(),
                        ContentReaction::getReaction
                ));
    }

    private DailyPopularContentResponseDto mapToDto(
            DailyPopularContent pc, Map<Integer, String> genreMap, ReactionType reactionType) {
        Content content = pc.getContent();

        // 장르 이름 합치기
        String genres = content.getGenreIds().stream()
                .map(genreMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(","));

        return DailyPopularContentResponseDto.of(pc, genres, reactionType);
    }

    @Override
    public ContentDetailDto getContentDetail(Long id, String type) {
        ContentId contentId = new ContentId(id, type);

        // 기본 정보와 장르 ID 조회
        Content content = contentRepository.findByIdWithGenres(contentId)
                .orElseThrow(() -> new RuntimeException("Content not found"));

        // 장르 정보 조회
        List<Genre> genres = genreRepository.findByIdIn(content.getGenreIds());

        // 출연진 정보 조회
        contentRepository.findByIdWithCasts(contentId);

        // 제작진 정보 조회
        contentRepository.findByIdWithCrews(contentId);

        // 비디오 정보 조회
        contentRepository.findByIdWithVideos(contentId);

        // 시청 제공자 정보 조회
        contentRepository.findByIdWithWatchProviders(contentId);

        return ContentDetailDto.of(content, genres);
    }

    @Override
    public List<ContentRecommendResponseDto> getContentRecommendList(Long userId, String type) {
        LocalDate today = LocalDate.now();
        String batchType = type == null ? null : type.trim().toUpperCase();

        DailyPopularContent popularContent =
                dailyPopularContentRepository.findFirstRanked(batchType, today);

        if(popularContent == null) {
            LocalDate yesterday = today.minusDays(1);
            popularContent = dailyPopularContentRepository.findFirstRanked(batchType, yesterday);
        }

        if (userId == null) {
            return contentRecommendationRepository.findWithoutUserReactions(popularContent.getContent());
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. userId: " + userId));

        return contentRecommendationRepository.findWithUserReactions(popularContent.getContent(), user.getUserId());
    }

    @Override
    public ContentListResponseDto_40 getContentPreferenceList(Long userId) {
        if(userId == null) {
            new BusinessException(ErrorCode.USER_NOT_FOUND, "존재하지 않는 사용자 ID");
        }
        List<String> titles = List.of("탑건", "라이언 일병 구하기", "범죄도시 4", "존 윅: 리로드", "나우 유 씨 미 2", "캐치 미 이프 유 캔", "500일의 썸머", "플립", "노트북", "어바웃 타임", "인셉션", "인 타임", "컨저링", "애나벨", "비긴 어게인", "스타 이즈 본", "보헤미안 랩소디", "어거스트 러쉬", "싱 스트리트", "셜록 홈즈", "명탐정 코난: 베이커가의 망령", "귀멸의 칼날", "블리치", "주술회전", "나루토", "태극기 휘날리며", "겨울왕국", "인사이드 아웃 2", "엘리멘탈", "드래곤 길들이기", "올드보이", "악마를 보았다", "극한직업", "엽기적인 그녀", "인턴", "더 울프 오브 월 스트리트", "에브리씽 에브리웨어 올 앳 원스", "어벤져스: 엔드게임", "저스티스 리그", "조커", "7번방의 선물");

        List<Content> contentList = contentRepository.findAllByTitleIn(titles);

        List<ContentSimpleResponseDto> contentDtoList = contentList.stream()
                .map(content -> ContentSimpleResponseDto.builder()
                        .id(content.getContentId().getId())
                        .type(content.getContentId().getType())
                        .title(content.getTitle())
                        .posterPath(content.getPosterPath())
                        .build())
                .collect(Collectors.toList());

        // 4. 변환된 DTO 리스트를 최종 DTO에 담아 반환
        return ContentListResponseDto_40.builder()
                .contents(contentDtoList)
                .build();
    }

    @Override
    public List<LikedContentResponseDto> getLikedContents(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. id : " + userId));

        List<ContentReaction> reactions = contentReactionRepository.findByUserAndReactionWithContent(user, ReactionType.LIKE);

        return reactions.stream()
                .map(LikedContentResponseDto::from)
                .collect(Collectors.toList());
    }
}
