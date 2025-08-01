package com.popcoclient.content.controller;

import com.popcoclient.common.response.ApiResponse;
import com.popcoclient.content.document.ContentDocument;
import com.popcoclient.content.service.ContentSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class ContentSearchController {

    private final ContentSearchService searchService;

    @GetMapping("/contents")
    public ResponseEntity<ApiResponse<Page<ContentDocument>>> searchContents(
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(searchService.searchContents(keyword, pageable)));
    }

    @GetMapping("/contents/advanced")
    public ResponseEntity<ApiResponse<List<ContentDocument>>> advancedSearch(
            @RequestParam String keyword,
            @RequestParam(required = false) String contentType) {
        return ResponseEntity.ok(ApiResponse.success(searchService.advancedSearch(keyword, contentType)));
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<ApiResponse<List<String>>> autocomplete(@RequestParam String prefix) {
        return ResponseEntity.ok(ApiResponse.success(searchService.autocomplete(prefix)));
    }
}