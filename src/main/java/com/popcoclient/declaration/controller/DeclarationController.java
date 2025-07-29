package com.popcoclient.declaration.controller;

import com.popcoclient.auth.jwt.JwtProvider;
import com.popcoclient.common.response.ApiResponse;
import com.popcoclient.declaration.dto.request.DeclarationCreateRequestDto;
import com.popcoclient.declaration.dto.response.DeclarationTypeResponseDto;
import com.popcoclient.declaration.service.DeclarationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "신고", description = "리뷰 신고 관련 API")
@RestController
@RequestMapping("/declarations")
@RequiredArgsConstructor
public class DeclarationController {
    private final DeclarationService declarationService;
    private final JwtProvider jwtProvider;


    @Operation(summary = "신고 타입 목록 조회", description = "리뷰 신고 시 사용할 수 있는 신고 유형 목록을 조회합니다.")
    @GetMapping("/type")
    public ResponseEntity<ApiResponse<List<DeclarationTypeResponseDto>>> getDeclarationTypes() {
        List<DeclarationTypeResponseDto> declarationTypes = declarationService.getDeclarationTypes();
        return ResponseEntity.ok(ApiResponse.success("Get DeclarationType Success",declarationTypes));
    }

    @Operation(summary = "리뷰 신고 생성", description = "특정 리뷰에 대해 신고를 생성합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> createDeclaration(@PathVariable Long reviewId,
                                                               @Valid @RequestBody DeclarationCreateRequestDto dto) {
        Long userId = jwtProvider.getRequiredUserId();
        declarationService.createReviewDeclaration(dto, userId, reviewId);
        return ResponseEntity.ok(ApiResponse.success("Create Declaration Success", null));
    }


}
