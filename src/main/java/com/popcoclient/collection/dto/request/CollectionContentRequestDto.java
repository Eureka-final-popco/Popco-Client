package com.popcoclient.collection.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionContentRequestDto {

    @NotNull(message = "컨텐츠 ID는 필수입니다")
    private Long contentId;

    @NotBlank(message = "컨텐츠 타입은 필수입니다")
    private String contentType;
}
