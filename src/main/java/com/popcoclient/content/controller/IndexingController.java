package com.popcoclient.content.controller;

import com.popcoclient.common.response.ApiResponse;
import com.popcoclient.content.service.ContentIndexingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/index")
@RequiredArgsConstructor
public class IndexingController {

    private final ContentIndexingService indexingService;

    @PostMapping("/reindex-all")
    public ResponseEntity<ApiResponse<String>> reindexAll() {
        indexingService.reindexAllContents();
        return ResponseEntity.ok(ApiResponse.success("Reindexing started"));
    }
}