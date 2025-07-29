package com.popcoclient.content.controller;

import com.popcoclient.auth.jwt.JwtProvider;
import com.popcoclient.common.response.ApiResponse;
import com.popcoclient.content.dto.response.ContentDetailDto;
import com.popcoclient.content.dto.response.ContentPageDto;
import com.popcoclient.content.dto.response.ContentRecommendResponseDto;
import com.popcoclient.content.dto.response.DailyPopularContentResponseDto;
import com.popcoclient.content.entity.Content;
import com.popcoclient.content.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "전체 콘텐츠 조회", description = "최근 발매된 작품부터 기본 40개씩 조회할 수 있다.")
    @GetMapping
    public ResponseEntity<ApiResponse<ContentPageDto>> getAllContents(
            @RequestParam(name = "pageNumber", defaultValue = "0") Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = "40") Integer pageSize,
            @RequestParam(name = "sort", defaultValue = "recent") String sort) {
        Sort sortOrder;
        if ("recent".equalsIgnoreCase(sort)) {
            sortOrder = Sort.by("releaseDate").descending();
        } else {
            String[] sortParams = sort.split(",");
            if (sortParams.length == 2) {
                String field = sortParams[0];
                Sort.Direction direction = Sort.Direction.fromString(sortParams[1]);
                sortOrder = Sort.by(direction, field);
            } else {
                sortOrder = Sort.by(sort);
            }
        }

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortOrder);

        Page<Content> contentsPage = contentService.getAllContents(pageable);

        ContentPageDto responseDto = new ContentPageDto(contentsPage);
        return ResponseEntity.ok(ApiResponse.success("전체 콘텐츠 조회 성공", responseDto));
    }

    @Operation(summary = "콘텐츠 일간랭킹", description = "ALL,MOVIE,TV 타입을 통해 사이트의 일간 랭킹을 확인할 수 있다.")
    @GetMapping("/popular/types/{type}")
    public ResponseEntity<List<DailyPopularContentResponseDto>> getPopularContent(@PathVariable String type) {
        List<DailyPopularContentResponseDto> response = contentService.getDailyPopularContentList(type);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "콘텐츠 상세 조회", description = "콘텐츠 상세 정보를 조회할 수 있다.")
    @GetMapping("/ids/{id}/types/{type}")
    public ResponseEntity<ApiResponse<ContentDetailDto>> getContent(@PathVariable Long id, @PathVariable String type) {
        ContentDetailDto response = contentService.getContentDetail(id, type);
        return ResponseEntity.ok(ApiResponse.success("콘텐츠 상세 조회에 성공했습니다.", response));
    }

    @Operation(summary = "1등과 관련된 콘텐츠", description = "ALL,MOVIE,TV 타입을 통해 1등과 관련된 콘텐츠를 확인할 수 있다..")
    @GetMapping("/popular/types/{type}/recommend")
    public ResponseEntity<ApiResponse<List<ContentRecommendResponseDto>>> getPopularRecommendContent(@PathVariable String type) {
        Long userId = jwtProvider.getNullableUserId();

        List<ContentRecommendResponseDto> response = contentService.getContentRecommendList(userId, type);
        return ResponseEntity.ok(ApiResponse.success("1등 관련 콘텐츠 조회에 성공했습니다.", response));
    }
}
