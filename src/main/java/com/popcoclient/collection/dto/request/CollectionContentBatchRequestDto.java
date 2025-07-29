package com.popcoclient.collection.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionContentBatchRequestDto {

    @NotEmpty(message = "컨텐츠 목록은 비어있을 수 없습니다")
    @Size(max = 100, message = "한 번에 최대 100개까지 추가할 수 있습니다")
    @Valid
    private List<CollectionContentRequestDto> contents;
}
