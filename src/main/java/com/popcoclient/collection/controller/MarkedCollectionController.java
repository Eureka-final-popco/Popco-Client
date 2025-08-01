package com.popcoclient.collection.controller;

import com.popcoclient.auth.jwt.JwtProvider;
import com.popcoclient.collection.dto.response.CollectionListResponseDto;
import com.popcoclient.collection.dto.response.CollectionResponseDto;
import com.popcoclient.collection.service.MarkedCollectionService;
import com.popcoclient.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "컬렉션 마크", description = "컬렉션 마크(북마크) 관련 API")
@RestController
@RequestMapping("/collections")
@RequiredArgsConstructor
public class MarkedCollectionController {

    private final JwtProvider jwtProvider;
    private final MarkedCollectionService markedCollectionService;

    // 컬렉션 마크/언마크 토글
    @Operation(summary = "컬렉션 마크 토글", description = "컬렉션을 마크하거나 마크를 해제합니다")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{collectionId}/mark")
    public ResponseEntity<ApiResponse<Boolean>> toggleMarkCollection(@PathVariable Long collectionId) {
        Long userId = jwtProvider.getUserIdFromAuthentication();
        boolean isMarked = markedCollectionService.toggleMarkCollection(userId, collectionId);
        String message = isMarked ? "컬렉션을 마크했습니다" : "컬렉션 마크를 해제했습니다";
        return ResponseEntity.ok(ApiResponse.success(message, isMarked));
    }

    // 컬렉션 마크
    @Operation(summary = "컬렉션 마크", description = "컬렉션을 마크합니다")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{collectionId}/mark")
    public ResponseEntity<ApiResponse<Void>> markCollection(@PathVariable Long collectionId) {
        Long userId = jwtProvider.getUserIdFromAuthentication();
        markedCollectionService.markCollection(userId, collectionId);
        return ResponseEntity.ok(ApiResponse.success("컬렉션을 마크했습니다", null));
    }

    // 컬렉션 언마크
    @Operation(summary = "컬렉션 언마크", description = "컬렉션 마크를 해제합니다")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{collectionId}/mark")
    public ResponseEntity<ApiResponse<Void>> unmarkCollection(@PathVariable Long collectionId) {
        Long userId = jwtProvider.getUserIdFromAuthentication();
        markedCollectionService.unmarkCollection(userId, collectionId);
        return ResponseEntity.ok(ApiResponse.success("컬렉션 마크를 해제했습니다", null));
    }

    // 컬렉션 마크 여부 확인
    @Operation(summary = "컬렉션 마크 여부 확인", description = "특정 컬렉션의 마크 여부를 확인합니다")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{collectionId}/mark")
    public ResponseEntity<ApiResponse<Boolean>> isMarkedCollection(@PathVariable Long collectionId) {
        Long userId = jwtProvider.getUserIdFromAuthentication();
        boolean isMarked = markedCollectionService.isMarkedByUser(userId, collectionId);
        return ResponseEntity.ok(ApiResponse.success("마크 여부 조회 성공", isMarked));
    }

    // 내가 마크한 컬렉션 목록
    @Operation(summary = "마크한 컬렉션 목록", description = "내가 마크한 컬렉션 목록을 조회합니다")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/marked")
    public ResponseEntity<ApiResponse<CollectionListResponseDto>> getMyMarkedCollections(
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = jwtProvider.getUserIdFromAuthentication();
        CollectionListResponseDto response = markedCollectionService.getUserMarkedCollections(userId, pageNumber, pageSize);
        return ResponseEntity.ok(ApiResponse.success("마크한 컬렉션 목록 조회 성공", response));
    }

    // 최근 일주일간 인기 컬렉션
    @Operation(summary = "주간 인기 컬렉션", description = "최근 일주일간 가장 많이 마크된 컬렉션 목록을 조회합니다. (로그인 비로그인 둘 다 가능하지만 로그인 상태일 때는 토큰 필요)")
    @GetMapping("/popular/weekly")
    public ResponseEntity<ApiResponse<List<CollectionResponseDto>>> getWeeklyPopularCollections(
            @RequestParam(defaultValue = "10") Integer limit) {
        Long userId = null;
        try {
            userId = jwtProvider.getUserIdFromAuthentication();
        } catch (Exception e) {
            // 로그인하지 않은 사용자도 조회 가능
        }
        List<CollectionResponseDto> response = markedCollectionService.getWeeklyPopularCollections(userId, limit);
        return ResponseEntity.ok(ApiResponse.success("주간 인기 컬렉션 조회 성공", response));
    }

    // 특정 컨텐츠를 포함한 컬렉션 목록
    @Operation(summary = "컨텐츠를 포함한 컬렉션", description = "특정 컨텐츠가 포함된 컬렉션 목록을 조회합니다. (로그인 비로그인 둘 다 가능하지만 로그인 상태일 때는 토큰 필요) (sortType 은 popular, latest 가 있습니다.)")
    @GetMapping("/content/{contentId}")
    public ResponseEntity<ApiResponse<CollectionListResponseDto>> getCollectionsByContent(
            @PathVariable Long contentId,
            @RequestParam String contentType,
            @RequestParam(defaultValue = "popular") String sortType,  // popular or latest
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = null;
        try {
            userId = jwtProvider.getUserIdFromAuthentication();
        } catch (Exception e) {
            // 로그인하지 않은 사용자도 조회 가능
        }
        CollectionListResponseDto response = markedCollectionService.getCollectionsByContent(
                contentId, contentType, sortType, userId, pageNumber, pageSize);
        return ResponseEntity.ok(ApiResponse.success("컨텐츠를 포함한 컬렉션 목록 조회 성공", response));
    }
}
