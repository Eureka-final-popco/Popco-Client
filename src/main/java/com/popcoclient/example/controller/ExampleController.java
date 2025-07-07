package com.popcoclient.example.controller;

import com.popcoclient.common.response.ApiResponse;
import com.popcoclient.example.dto.response.ExampleResponseDto;
import com.popcoclient.example.service.ExampleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/example")
@Tag(name = "예시 API", description = "예시 패키지 컨트롤러, 배포 전 삭제 예정")
@RequiredArgsConstructor
public class ExampleController {
    private final ExampleService exampleService;

    @Operation(summary = "성공 응답 예시", description = "단일 Dto 응답")
    @GetMapping("/success")
    public ResponseEntity<ApiResponse<ExampleResponseDto>> getExample() {
        return ResponseEntity.ok(ApiResponse.success(exampleService.getExample()));
    }

    // 응답 예시
//    {
//        "code":200,
//            "result":"SUCCESS",
//            "message":"요청이 성공적으로 처리되었습니다.",
//            "data":{
//                "id":1,
//                "description":"성공 응답 예시"
//            }
//    }

    @Operation(summary = "예외 응답 예시", description = "예외 테스트 API")
    @GetMapping("/exception")
    public ResponseEntity<ApiResponse<ExampleResponseDto>> getException() {
        return ResponseEntity.ok(ApiResponse.success(exampleService.getException()));
    }

    // 응답 예시
//    {
//        "code": 404,
//            "result": "USER_NOT_FOUND",
//            "message": "사용자를 찾을 수 없습니다.",
//            "data": null
//    }

    @Operation(summary = "리스트 응답 예시", description = "복수 Dto 응답")
    @GetMapping("/list")
    // 페이징 사이즈 방식은 프론트와 논의 후 결정
    public ResponseEntity<ApiResponse<List<ExampleResponseDto>>> getExampleList(
            @PageableDefault(size = 8, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(exampleService.getExampleList(pageable)));
    }

    // 응답 예시
//    {
//        "code": 200,
//        "result": "SUCCESS",
//        "message": "요청이 성공적으로 처리되었습니다.",
//        "data": [
//              {
//                "id": 1,
//                "description": "리스트 응답 예시"
//              }
//       ]
//    }

}
