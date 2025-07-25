package com.popcoclient.declaration.controller;

import com.popcoclient.auth.jwt.JwtProvider;
import com.popcoclient.common.response.ApiResponse;
import com.popcoclient.declaration.dto.request.DeclarationCreateRequestDto;
import com.popcoclient.declaration.dto.response.DeclarationTypeResponseDto;
import com.popcoclient.declaration.service.DeclarationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/declaration")
@RequiredArgsConstructor
public class DeclarationController {
    private final DeclarationService declarationService;
    private final JwtProvider jwtProvider;

    @GetMapping("/type")
    public ResponseEntity<ApiResponse<List<DeclarationTypeResponseDto>>> getDeclarationTypes() {
        List<DeclarationTypeResponseDto> declarationTypes = declarationService.getDeclarationTypes();
        return ResponseEntity.ok(ApiResponse.success("Get DeclarationType Success",declarationTypes));
    }

    @PostMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> createDeclaration(@PathVariable Long reviewId,
                                                               @Valid @RequestBody DeclarationCreateRequestDto dto) {
        Long userId = jwtProvider.getUserIdFromAuthentication();
        declarationService.createReviewDeclaration(dto, userId, reviewId);
        return ResponseEntity.ok(ApiResponse.success("Create Declaration Success", null));
    }


}
