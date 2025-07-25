package com.popcoclient.declaration.service;

import com.popcoclient.declaration.dto.request.DeclarationCreateRequestDto;
import com.popcoclient.declaration.dto.response.DeclarationTypeResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;

public interface DeclarationService {
    List<DeclarationTypeResponseDto> getDeclarationTypes();
    void createReviewDeclaration(DeclarationCreateRequestDto dto, Long userId, Long reviewerId);
}
