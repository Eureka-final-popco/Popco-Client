package com.popcoclient.collection.controller;

import com.popcoclient.auth.jwt.JwtProvider;
import com.popcoclient.collection.dto.request.CollectionContentRequestDto;
import com.popcoclient.collection.dto.response.CollectionContentResponseDto;
import com.popcoclient.collection.service.CollectionContentService;
import com.popcoclient.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "컬렉션 컨텐츠", description = "컬렉션에서 컨텐츠와 관련된 api")
@RestController
@RequestMapping("/collections/{collectionId}/contents")
@RequiredArgsConstructor
public class CollectionContentController {

    private final JwtProvider jwtProvider;

    private final CollectionContentService collectionContentService;

    // 컬렉션에 컨텐츠 추가
    @Operation(summary = "컬렉션에 컨텐츠 추가", description = "")
    @PostMapping
    public ResponseEntity<ApiResponse<CollectionContentResponseDto>> addContentToCollection(@PathVariable Long collectionId, @Valid @RequestBody CollectionContentRequestDto request) {
        Long userId = jwtProvider.getUserIdFromAuthentication();
        CollectionContentResponseDto response = collectionContentService.addContentToCollection(userId, collectionId, request);
        return ResponseEntity.ok(ApiResponse.success("컬렉션에 컨텐츠 추가 성공", response));
    }

    // 컬렉션의 컨텐츠 목록 조회 (페이징)
    @Operation(summary = "컬렉션의 컨텐츠 목록 조회 (페이징)", description = "")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CollectionContentResponseDto>>> getCollectionContents(@PathVariable Long collectionId, @RequestParam Integer pageNumber, @RequestParam Integer pageSize) {
        Page<CollectionContentResponseDto> response = collectionContentService.getCollectionContents(collectionId, pageNumber, pageSize);
        return ResponseEntity.ok(ApiResponse.success("컬렉션의 컨텐츠 목록 조회 성공", response));
    }

    // 컬렉션의 모든 컨텐츠 조회
    @Operation(summary = "컬렉션의 모든 컨텐츠 조회", description = "")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<CollectionContentResponseDto>>> getAllCollectionContents(@PathVariable Long collectionId) {
        List<CollectionContentResponseDto> response = collectionContentService.getAllCollectionContents(collectionId);
        return ResponseEntity.ok(ApiResponse.success("컬렉션의 모든 컨텐츠 조회 성공", response));
    }

    // 컬렉션에서 컨텐츠 제거
    @Operation(summary = "컬렉션에서 컨텐츠 제거", description = "")
    @DeleteMapping("/{contentId}")
    public ResponseEntity<ApiResponse<Void>> removeContentFromCollection(@PathVariable Long collectionId, @PathVariable Long contentId, @RequestParam String contentType) {
        Long userId = jwtProvider.getUserIdFromAuthentication();
        collectionContentService.removeContentFromCollection(userId, collectionId, contentId, contentType);
        return ResponseEntity.ok(ApiResponse.success("컬렉션에서 컨텐츠 삭제 성공", null));
    }

    // 컬렉션의 컨텐츠 개수 조회
    @Operation(summary = "컬렉션의 컨텐츠 개수 조회", description = "")
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getCollectionContentCount(@PathVariable Long collectionId) {
        long count = collectionContentService.getCollectionContentCount(collectionId);
        return ResponseEntity.ok(ApiResponse.success("컬렉션의 컨텐츠 개수 조회 성공", count));
    }

    // 특정 컨텐츠가 컬렉션에 포함되어 있는지 확인
    @Operation(summary = "특정 컨텐츠가 컬렉션에 포함되어 있는지 확인", description = "")
    @GetMapping("/check/{contentId}")
    public ResponseEntity<ApiResponse<Boolean>> isContentInCollection(@PathVariable Long collectionId, @PathVariable Long contentId, @RequestParam String contentType) {
        boolean exists = collectionContentService.isContentInCollection(collectionId, contentId, contentType);
        return ResponseEntity.ok(ApiResponse.success("특정 컨텐츠가 컬렉션에 포함되어 있는지 확인 성공", exists));
    }
}
