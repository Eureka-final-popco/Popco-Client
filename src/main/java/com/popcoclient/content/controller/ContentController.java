package com.popcoclient.content.controller;

import com.popcoclient.content.dto.response.DailyPopularContentResponseDto;
import com.popcoclient.content.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "콘텐츠", description = "상세 페이지, 메인 페이지에 사용되는 전반적인 콘텐츠 API")
@RestController
@RequestMapping("/contents")
@RequiredArgsConstructor
public class ContentController {
    private final ContentService contentService;

    @Operation(summary = "콘텐츠 일간랭킹", description = "ALL,MOVIE,TV 타입을 통해 사이트의 일간 랭킹을 확인할 수 있다.")
    @GetMapping("/popular/type/{type}")
    public ResponseEntity<List<DailyPopularContentResponseDto>> getContent(@PathVariable String type) {
        List<DailyPopularContentResponseDto> response = contentService.getDailyPopularContent(type);
        return ResponseEntity.ok(response);
    }
}
