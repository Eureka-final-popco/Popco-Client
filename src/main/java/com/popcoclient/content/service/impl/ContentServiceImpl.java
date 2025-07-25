package com.popcoclient.content.service.impl;

import com.popcoclient.content.dto.response.*;
import com.popcoclient.content.entity.*;
import com.popcoclient.content.entity.key.ContentId;
import com.popcoclient.content.repository.ContentRepository;
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
    private final ContentRepository contentRepository;

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

        return convertToDto(content, genres);
    }

    private ContentDetailDto convertToDto(Content content, List<Genre> genres) {
        return ContentDetailDto.builder()
                .id(content.getContentId().getId())
                .type(content.getContentId().getType())
                .title(content.getTitle())
                .overview(content.getOverview())
                .ratingAverage(content.getRatingAverage())
                .releaseDate(content.getReleaseDate())
                .ratingCount(content.getRatingCount())
                .backdropPath(content.getBackdropPath())
                .posterPath(content.getPosterPath())
                .runtime(content.getRuntime())
                .genres(convertGenresToDto(genres))
                .casts(convertCastsToDto(content.getCasts()))
                .crews(convertCrewsToDto(content.getCrews()))
                .videos(convertVideosToDto(content.getVideos()))
                .watchProviders(convertWatchProvidersToDto(content.getWatchProviders()))
                .build();
    }

    private List<GenreDto> convertGenresToDto(List<Genre> genres) {
        return genres.stream()
                .map(genre -> GenreDto.builder()
                        .id(genre.getId())
                        .name(genre.getName())
                        .build())
                .collect(Collectors.toList());
    }

    private List<CastMemberDto> convertCastsToDto(List<CastMember> casts) {
        return casts.stream()
                .map(cast -> CastMemberDto.builder()
                        .actorId(cast.getActor().getId())
                        .actorName(cast.getActor().getName())
                        .profilePath(cast.getActor().getProfilePath())
                        .characterName(cast.getCharacterName())
                        .castOrder(cast.getCastOrder())
                        .build())
                .sorted((a, b) -> Integer.compare(a.getCastOrder(), b.getCastOrder()))
                .collect(Collectors.toList());
    }

    private List<CrewDto> convertCrewsToDto(List<Crew> crews) {
        return crews.stream()
                .map(crew -> CrewDto.builder()
                        .crewMemberId(crew.getCrewMember().getId())
                        .name(crew.getCrewMember().getName())
                        .profilePath(crew.getCrewMember().getProfilePath())
                        .job(crew.getJob())
                        .knownForDepartment(crew.getCrewMember().getKnownForDepartment())
                        .build())
                .collect(Collectors.toList());
    }

    private List<VideoDto> convertVideosToDto(List<ContentVideo> videos) {
        return videos.stream()
                .map(video -> VideoDto.builder()
                        .id(video.getId())
                        .name(video.getName())
                        .key(video.getKey())
                        .type(video.getType())
                        .official(video.getOfficial())
                        .build())
                .collect(Collectors.toList());
    }

    private List<WatchProviderDto> convertWatchProvidersToDto(List<WatchProvider> watchProviders) {
        return watchProviders.stream()
                .map(wp -> WatchProviderDto.builder()
                        .providerId(wp.getProvider().getId())
                        .name(wp.getProvider().getName())
                        .link(wp.getProvider().getLink())
                        .logoPath(wp.getProvider().getLogoPath())
                        .build())
                .collect(Collectors.toList());
    }
}
