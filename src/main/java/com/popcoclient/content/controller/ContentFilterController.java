package com.popcoclient.content.controller;

import com.popcoclient.common.response.ApiResponse;
import com.popcoclient.content.dto.request.FilterRequestDto;
import com.popcoclient.content.dto.response.PaginatedContentFilterResponseDto;
import com.popcoclient.content.service.ContentFilterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/contents/filters")
@RequiredArgsConstructor
@Tag(name = "콘텐츠 필터링", description = "콘텐츠 필터링 및 검색 API")
public class ContentFilterController {

    private final ContentFilterService contentFilterService;

    @Operation(
            summary = "콘텐츠 필터링 조회",
            description = "조건에 맞는 콘텐츠를 필터링하여 페이징 결과를 반환합니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<PaginatedContentFilterResponseDto>> filterContents(
            @RequestBody FilterRequestDto filterRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "40") int size,
            @RequestHeader(value = "X-User-ID", required = false) Long userId
    ) {
        PaginatedContentFilterResponseDto filteredData = contentFilterService.filterContents(
                filterRequest.getContentType(),
                filterRequest.getGenres(),
                filterRequest.getMinRating(),
                filterRequest.getMaxRating(),
                filterRequest.getPlatforms(),
                filterRequest.getMinReleaseYear(),
                filterRequest.getMaxReleaseYear(),
                filterRequest.getAgeGroupFilter(),
                filterRequest.getPersonaFilter(),
                filterRequest.getPopcorithmFilter(),
                userId,
                page,
                size
        );

        return ResponseEntity.ok(ApiResponse.success("콘텐츠 필터링 성공", filteredData));
    }
}
