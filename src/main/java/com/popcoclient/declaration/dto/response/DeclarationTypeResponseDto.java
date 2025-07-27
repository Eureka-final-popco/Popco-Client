package com.popcoclient.declaration.dto.response;

import com.popcoclient.declaration.entity.enums.DeclarationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeclarationTypeResponseDto {
    private String code;
    private String description;

    public static DeclarationTypeResponseDto from(DeclarationType declarationType) {
        return DeclarationTypeResponseDto.builder()
                .code(declarationType.name())
                .description(declarationType.getDescription())
                .build();
    }
}
