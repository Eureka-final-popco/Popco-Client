package com.popcoclient.collection.controller;

import com.popcoclient.auth.jwt.JwtProvider;
import com.popcoclient.collection.dto.request.CollectionRequestDto;
import com.popcoclient.collection.dto.request.CollectionUpdateRequestDto;
import com.popcoclient.collection.dto.response.CollectionListResponseDto;
import com.popcoclient.collection.dto.response.CollectionResponseDto;
import com.popcoclient.collection.service.CollectionService;
import com.popcoclient.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "컬렉션", description = "컬렉션과 관련된 api")
@RestController
@RequestMapping("/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final JwtProvider jwtProvider;
    private final CollectionService collectionService;

    // 컬렉션 생성
    @Operation(summary = "컬렉션 생성", description = "새로운 컬렉션을 생성합니다")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<ApiResponse<CollectionResponseDto>> createCollection(@Valid @RequestBody CollectionRequestDto request) {
        Long userId = jwtProvider.getRequiredUserId();
        CollectionResponseDto response = collectionService.createCollection(userId, request);
        return ResponseEntity.ok(ApiResponse.success("컬렉션 생성 성공", response));
    }

    // 특정 컬렉션 조회
    @Operation(summary = "특정 컬렉션 조회", description = "컬렉션 ID로 특정 컬렉션을 조회합니다")
    @GetMapping("/{collectionId}")
    public ResponseEntity<ApiResponse<CollectionResponseDto>> getCollection(@PathVariable Long collectionId) {
        Long userId = null;
        try {
            userId = jwtProvider.getNullableUserId();
        } catch (Exception e) {
            // 로그인하지 않은 사용자도 조회 가능
        }

        CollectionResponseDto response = userId != null ?
                collectionService.getCollection(collectionId, userId) :
                collectionService.getCollection(collectionId);

        return ResponseEntity.ok(ApiResponse.success("특정 컬렉션 조회 성공", response));
    }

    // 사용자의 컬렉션 목록 조회
    @Operation(summary = "사용자의 컬렉션 목록 조회", description = "특정 사용자의 컬렉션 목록을 조회합니다")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<CollectionListResponseDto>> getUserCollections(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long currentUserId = null;
        try {
            currentUserId = jwtProvider.getNullableUserId();
        } catch (Exception e) {
            // 로그인하지 않은 사용자도 조회 가능
        }

        CollectionListResponseDto response = currentUserId != null ?
                collectionService.getUserCollections(userId, currentUserId, pageNumber, pageSize) :
                collectionService.getUserCollections(userId, pageNumber, pageSize);

        return ResponseEntity.ok(ApiResponse.success("특정 사용자의 컬렉션 목록 조회 성공", response));
    }

    // 내 컬렉션 목록 조회
    @Operation(summary = "내 컬렉션 목록 조회", description = "현재 로그인한 사용자의 컬렉션 목록을 조회합니다")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<CollectionListResponseDto>> getMyCollections(
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = jwtProvider.getRequiredUserId();
        CollectionListResponseDto response = collectionService.getUserCollections(userId, pageNumber, pageSize);
        return ResponseEntity.ok(ApiResponse.success("내 컬렉션 목록 조회 성공", response));
    }

    // 컬렉션 검색
    @Operation(summary = "컬렉션 검색", description = "키워드로 컬렉션을 검색합니다")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<CollectionListResponseDto>> searchCollections(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = null;
        try {
            userId = jwtProvider.getNullableUserId();
        } catch (Exception e) {
            // 로그인하지 않은 사용자도 조회 가능
        }

        CollectionListResponseDto response = userId != null ?
                collectionService.searchCollections(keyword, userId, pageNumber, pageSize) :
                collectionService.searchCollections(keyword, pageNumber, pageSize);

        return ResponseEntity.ok(ApiResponse.success("컬렉션 검색 성공", response));
    }

    // 전체 컬렉션 목록
    @Operation(summary = "전체 컬렉션 목록 조회", description = "전체 컬렉션 목록을 페이징하여 조회합니다")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CollectionResponseDto>>> getCollections(
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = null;
        try {
            userId = jwtProvider.getNullableUserId();
        } catch (Exception e) {
            // 로그인하지 않은 사용자도 조회 가능
        }

        List<CollectionResponseDto> response = userId != null ?
                collectionService.getCollections(userId, pageNumber, pageSize) :
                collectionService.getCollections(pageNumber, pageSize);

        return ResponseEntity.ok(ApiResponse.success("컬렉션 목록 조회 성공", response));
    }

    // 인기 컬렉션 목록 조회
//    @Operation(summary = "인기 컬렉션 목록 조회", description = "저장 수가 많은 상위 10개 컬렉션을 조회합니다")
//    @GetMapping("/popular")
//    public ResponseEntity<ApiResponse<List<CollectionResponseDto>>> getPopularCollections() {
//        Long userId = null;
//        try {
//            userId = jwtProvider.getUserIdFromAuthentication();
//        } catch (Exception e) {
//            // 로그인하지 않은 사용자도 조회 가능
//        }
//
//        List<CollectionResponseDto> response = userId != null ?
//                collectionService.getPopularCollections(userId) :
//                collectionService.getPopularCollections();
//
//        return ResponseEntity.ok(ApiResponse.success("인기 컬렉션 목록 조회 성공", response));
//    }

    // 컬렉션 수정
    @Operation(summary = "컬렉션 수정", description = "컬렉션의 제목과 설명을 수정합니다")
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{collectionId}")
    public ResponseEntity<ApiResponse<CollectionResponseDto>> updateCollection(
            @PathVariable Long collectionId,
            @Valid @RequestBody CollectionUpdateRequestDto request) {
        Long userId = jwtProvider.getRequiredUserId();
        CollectionResponseDto response = collectionService.updateCollection(userId, collectionId, request);
        return ResponseEntity.ok(ApiResponse.success("컬렉션 수정 성공", response));
    }

    // 컬렉션 삭제
    @Operation(summary = "컬렉션 삭제", description = "컬렉션을 삭제합니다")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{collectionId}")
    public ResponseEntity<ApiResponse<Void>> deleteCollection(@PathVariable Long collectionId) {
        Long userId = jwtProvider.getRequiredUserId();
        collectionService.deleteCollection(userId, collectionId);
        return ResponseEntity.ok(ApiResponse.success("컬렉션 삭제 성공", null));
    }
}
