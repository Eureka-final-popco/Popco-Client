package com.popcoclient.user.controller;

import com.popcoclient.common.response.ApiResponse;
import com.popcoclient.user.dto.request.WishListRequestDto;
import com.popcoclient.user.dto.response.WishListResponseDto;
import com.popcoclient.user.entity.WishList;
import com.popcoclient.user.service.WishListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/wishlists")
@RequiredArgsConstructor
@Tag(name = "위시리스트 API", description = "위시리스트 관련 API")
public class WishListController {
    private final WishListService wishListService;

    @Operation(summary = "위시리스트에 콘텐츠 추가", description = "지정된 사용자의 위시리스트에 콘텐츠를 추가합니다.")
    @PostMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<WishListResponseDto>> createWishList(
            @PathVariable Long userId,
            @RequestBody WishListRequestDto request) {
     WishList wishList = wishListService.addWishList(userId, request.getContentId(), request.getContentType());
     return ResponseEntity.ok(ApiResponse.success("위시리스트에 콘텐츠 추가 성공", WishListResponseDto.from(wishList)));
    }

    @Operation(summary = "사용자 위시리스트 전체 조회", description = "지정된 사용자의 위시리스트 항목을 모두 조회합니다.")
    @GetMapping("users/{userId}")
    public ResponseEntity<ApiResponse<List<WishListResponseDto>>> getWishLists(@PathVariable Long userId) {
        List<WishList> wishLists = wishListService.getWishLists(userId);

        List<WishListResponseDto> response = wishLists.stream()
                .map(WishListResponseDto::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("위시리스트 목록 조회 성공", response));
    }

    @Operation(summary = "위시리스트에서 콘텐츠 삭제", description = "지정된 사용자의 위시리스트에서 특정 콘텐츠를 삭제합니다.")
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteWishList(
            @PathVariable Long userId,
            @RequestBody WishListRequestDto request) {
        wishListService.deleteWishList(userId, request.getContentId(), request.getContentType());
        return ResponseEntity.ok(ApiResponse.success("위시리스트 콘텐츠 삭제 성공", null));
    }
}
