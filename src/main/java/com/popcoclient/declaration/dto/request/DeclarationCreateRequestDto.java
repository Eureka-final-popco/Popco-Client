package com.popcoclient.declaration.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeclarationCreateRequestDto {
    private String declarationType;
    private String content;
}
