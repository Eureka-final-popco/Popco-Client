package com.popcoclient.persona.dto.response;

import com.popcoclient.persona.entity.Option;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OptionResponseDto {
    private Long optionId;
    private String content;

    public static OptionResponseDto from(Option option) {
        return OptionResponseDto.builder()
                .optionId(option.getOptionsId().getOptionId())
                .content(option.getContent())
                .build();
    }
}
