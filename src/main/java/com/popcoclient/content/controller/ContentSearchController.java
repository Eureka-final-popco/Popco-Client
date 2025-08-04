package com.popcoclient.content.controller;

import com.popcoclient.auth.jwt.JwtProvider;
import com.popcoclient.common.response.ApiResponse;
import com.popcoclient.content.document.ContentDocument;
import com.popcoclient.content.dto.response.AutocompleteResponse;
import com.popcoclient.content.dto.response.ContentSearchResponse;
import com.popcoclient.content.service.ContentSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "검색", description = "제목, 배우, 줄거리로 검색")
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class ContentSearchController {

    private final JwtProvider jwtProvider;
    private final ContentSearchService searchService;

    @Operation(summary = "검색", description = "제목으로 컨텐츠 검색을 할 수 있다. (강화된 검색 사용하시면 될거 같습니다.)")
    @GetMapping("/contents")
    public ResponseEntity<ApiResponse<Page<ContentDocument>>> searchContents(
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(searchService.searchContents(keyword, pageable)));
    }

    @Operation(summary = "강화된 검색", description = "제목, 배우, 줄거리 키워드로 컨텐츠 검색을 할 수 있다. contentType 은 안넣어도 됩니다. 키워드 검색할 때는 keyword만 쓰시고, 배우 조합으로 검색할 때는 actors 만 쓰시면 됩니다. (pageable 예시 : {page: 0, size: 2})")
    @GetMapping("/contents/advanced")
    public ResponseEntity<Page<ContentSearchResponse>> advancedSearch(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String contentType,
            @RequestParam(required = false) List<String> actors,
            @PageableDefault(size = 20) Pageable pageable) {

        // 로그인 여부 확인
        Long userId = null;
        try {
            userId = jwtProvider.getNullableUserId();
        } catch (Exception e) {
            // 로그인하지 않은 사용자도 검색 가능
        }

        return ResponseEntity.ok(searchService.advancedSearch(keyword, contentType, actors, userId, pageable));
    }

    @Operation(summary = "검색어 추천 목록", description = "prefix로 검색어 추천 목록을 제공합니다. 검색어에 한글자 입력될 때마다 사용되면 되려나요..")
    @GetMapping("/autocomplete")
    public ResponseEntity<ApiResponse<List<AutocompleteResponse>>> autocomplete(@RequestParam String prefix) {
        return ResponseEntity.ok(ApiResponse.success(searchService.autocomplete(prefix)));
    }
}