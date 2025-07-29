package com.popcoclient.content.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ContentListResponseDto_40 { // 선호도 진단 시, 40개의 리스트를 보내주는 dto
    List<ContentSimpleResponseDto> contents;
}
