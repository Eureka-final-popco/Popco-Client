package com.popcoclient.content.controller;

import com.popcoclient.auth.jwt.JwtProvider;
import com.popcoclient.common.response.ApiResponse;
import com.popcoclient.content.dto.response.*;
import com.popcoclient.content.entity.Content;
import com.popcoclient.content.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "콘텐츠", description = "상세 페이지, 메인 페이지에 사용되는 전반적인 콘텐츠 API")
@RestController
@RequestMapping("/contents")
@RequiredArgsConstructor
public class ContentController {
    private final ContentService contentService;
    private final JwtProvider jwtProvider;

    @Operation(summary = "전체 콘텐츠 조회",description = "id 오름차순, 최신순, 인기순 각 기본 40개씩 조회할 수 있다.")
    @GetMapping
    public ResponseEntity<ApiResponse<ContentPageDto>> getAllContents(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "40") Integer size,
            @Parameter(description = "정렬 기준: 'recent' (최신순), 'popular' (인기순), 'id_asc' (ID 오름차순). 기본값은 'id_asc'")
            @RequestParam(name = "sort", defaultValue = "id_asc") String sortType,
            @RequestHeader(value = "X-User-Id", required = false) Long userId
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.unsorted());

        ContentPageDto responseDto = contentService.getAllContents(pageable, sortType, userId);

        return ResponseEntity.ok(ApiResponse.success("전체 콘텐츠 조회 성공", responseDto));
    }

    @Operation(summary = "콘텐츠 주간랭킹", description = "ALL,MOVIE,TV 타입을 통해 사이트의 일간 랭킹을 확인할 수 있다.")
    @GetMapping("/popular/types/{type}")
    public ResponseEntity<ApiResponse<List<DailyPopularContentResponseDto>>> getPopularContent(@PathVariable String type) {
        Long userId = jwtProvider.getNullableUserId();

        List<DailyPopularContentResponseDto> response = contentService.getDailyPopularContentList(userId, type);
        return ResponseEntity.ok(ApiResponse.success("일간 랭킹 조회에 성공했습니다.", response));
    }

    @Operation(summary = "콘텐츠 상세 조회", description = "콘텐츠 상세 정보를 조회할 수 있다. 로그인한 사용자의 경우 좋아요/싫어요 여부가 포함된다.")
    @GetMapping("/ids/{id}/types/{type}")
    public ResponseEntity<ApiResponse<ContentDetailDto>> getContent(
            @PathVariable Long id,
            @PathVariable String type) {

        // 로그인 여부 확인
        Long userId = null;
        try {
            userId = jwtProvider.getNullableUserId();
        } catch (Exception e) {
            // 로그인하지 않은 사용자도 조회 가능
        }

        ContentDetailDto response = contentService.getContentDetail(id, type, userId);
        return ResponseEntity.ok(ApiResponse.success("콘텐츠 상세 조회에 성공했습니다.", response));
    }

    @Operation(summary = "1등과 관련된 콘텐츠", description = "ALL,MOVIE,TV 타입을 통해 1등과 관련된 콘텐츠를 확인할 수 있다..")
    @GetMapping("/popular/types/{type}/recommend")
    public ResponseEntity<ApiResponse<List<ContentRecommendResponseDto>>> getPopularRecommendContent(@PathVariable String type) {
        Long userId = jwtProvider.getNullableUserId();

        List<ContentRecommendResponseDto> response = contentService.getContentRecommendList(userId, type);
        return ResponseEntity.ok(ApiResponse.success("1등 관련 콘텐츠 조회에 성공했습니다.", response));
    }

    @Operation(summary = "선호도 진단 시 포스터 조회", description = "선호도 진단에 쓰이는 40개의 포스터를 조회하는 api")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/preferences")
    public ResponseEntity<ApiResponse<ContentListResponseDto_40>> getContentPreferenceTest() {
        Long userId = jwtProvider.getRequiredUserId();
        return ResponseEntity.ok(ApiResponse.success(contentService.getContentPreferenceList(userId)));
    }

    @Operation(summary = "마이페이지 내가 좋아요한 컨텐츠 목록", description = "내가 좋아요 누른 컨텐츠 목록 조회")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/liked")
    public ResponseEntity<ApiResponse<List<LikedContentResponseDto>>> getLikedContents() {
        Long userId = jwtProvider.getRequiredUserId();
        List<LikedContentResponseDto> likedContents = contentService.getLikedContents(userId);
        return ResponseEntity.ok(ApiResponse.success("내가 좋아요 누른 컨텐츠 목록 조회 성공", likedContents));
    }
}
