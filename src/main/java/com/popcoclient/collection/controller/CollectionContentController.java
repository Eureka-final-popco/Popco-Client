package com.popcoclient.collection.controller;

import com.popcoclient.auth.jwt.JwtProvider;
import com.popcoclient.collection.dto.request.CollectionContentBatchRequestDto;
import com.popcoclient.collection.dto.request.CollectionContentRequestDto;
import com.popcoclient.collection.dto.response.CollectionContentBatchResponseDto;
import com.popcoclient.collection.dto.response.CollectionContentResponseDto;
import com.popcoclient.collection.service.CollectionContentService;
import com.popcoclient.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
    @Operation(summary = "컬렉션에 컨텐츠 추가", description = "컬렉션에 컨텐츠를 추가합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<ApiResponse<CollectionContentResponseDto>> addContentToCollection(@PathVariable Long collectionId, @Valid @RequestBody CollectionContentRequestDto request) {
        Long userId = jwtProvider.getRequiredUserId();
        CollectionContentResponseDto response = collectionContentService.addContentToCollection(userId, collectionId, request);
        return ResponseEntity.ok(ApiResponse.success("컬렉션에 컨텐츠 추가 성공", response));
    }

    // 컬렉션에 여러 컨텐츠 한번에 추가
    @Operation(summary = "컬렉션에 여러 컨텐츠 추가", description = "한 번에 여러 개의 컨텐츠를 컬렉션에 추가합니다")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<CollectionContentBatchResponseDto>> addMultipleContentsToCollection(
            @PathVariable Long collectionId,
            @Valid @RequestBody CollectionContentBatchRequestDto request) {
        Long userId = jwtProvider.getRequiredUserId();
        CollectionContentBatchResponseDto response = collectionContentService.addMultipleContentsToCollection(userId, collectionId, request);
        return ResponseEntity.ok(ApiResponse.success("컬렉션에 여러 컨텐츠 추가 완료", response));
    }

    // 컬렉션의 컨텐츠 목록 조회 (페이징)
    @Operation(summary = "컬렉션의 컨텐츠 목록 조회 (페이징)", description = "컬렉션의 컨텐츠 목록을 페이징하여 가져옵니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CollectionContentResponseDto>>> getCollectionContents(@PathVariable Long collectionId, @RequestParam Integer pageNumber, @RequestParam Integer pageSize) {
        Page<CollectionContentResponseDto> response = collectionContentService.getCollectionContents(collectionId, pageNumber, pageSize);
        return ResponseEntity.ok(ApiResponse.success("컬렉션의 컨텐츠 목록 조회 성공", response));
    }

    // 컬렉션의 모든 컨텐츠 조회
    @Operation(summary = "컬렉션의 모든 컨텐츠 조회", description = "컬렉션의 모든 컨텐츠를 가져옵니다.")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<CollectionContentResponseDto>>> getAllCollectionContents(@PathVariable Long collectionId) {
        List<CollectionContentResponseDto> response = collectionContentService.getAllCollectionContents(collectionId);
        return ResponseEntity.ok(ApiResponse.success("컬렉션의 모든 컨텐츠 조회 성공", response));
    }

    // 컬렉션에서 컨텐츠 제거
    @Operation(summary = "컬렉션에서 컨텐츠 제거", description = "컬렉션에서 컨텐츠를 제거합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{contentId}")
    public ResponseEntity<ApiResponse<Void>> removeContentFromCollection(@PathVariable Long collectionId, @PathVariable Long contentId, @RequestParam String contentType) {
        Long userId = jwtProvider.getRequiredUserId();
        collectionContentService.removeContentFromCollection(userId, collectionId, contentId, contentType);
        return ResponseEntity.ok(ApiResponse.success("컬렉션에서 컨텐츠 삭제 성공", null));
    }

    // 컬렉션의 컨텐츠 개수 조회
    @Operation(summary = "컬렉션의 컨텐츠 개수 조회", description = "컬렉션의 컨텐츠 개수를 조회합니다.")
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getCollectionContentCount(@PathVariable Long collectionId) {
        long count = collectionContentService.getCollectionContentCount(collectionId);
        return ResponseEntity.ok(ApiResponse.success("컬렉션의 컨텐츠 개수 조회 성공", count));
    }

    // 특정 컨텐츠가 컬렉션에 포함되어 있는지 확인
    @Operation(summary = "특정 컨텐츠가 컬렉션에 포함되어 있는지 확인", description = "특정 컨텐츠가 컬렉션에 포함되어 있는지 확인합니다. 컬렉션에 추가적으로 컨텐츠를 추가할 때 사용하면 좋을 거 같습니다.")
    @GetMapping("/check/{contentId}")
    public ResponseEntity<ApiResponse<Boolean>> isContentInCollection(@PathVariable Long collectionId, @PathVariable Long contentId, @RequestParam String contentType) {
        boolean exists = collectionContentService.isContentInCollection(collectionId, contentId, contentType);
        return ResponseEntity.ok(ApiResponse.success("특정 컨텐츠가 컬렉션에 포함되어 있는지 확인 성공", exists));
    }
}
