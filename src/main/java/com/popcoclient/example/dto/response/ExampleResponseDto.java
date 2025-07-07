package com.popcoclient.example.dto.response;

import com.popcoclient.example.entity.Example;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExampleResponseDto {
    private Long id;
    private String description;

    public static ExampleResponseDto from(Example example, String description){
        return ExampleResponseDto.builder()
                .id(example.getId())   // Entity 에서 가져올 수 있는 정보
                .description(description) // Dto 에서 추가로 필요한 정보
                .build();
    }
}
