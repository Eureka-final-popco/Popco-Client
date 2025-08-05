package com.popcoclient.event.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitAnswerRequestDto {
    
    @NotNull(message = "선택지 ID는 필수입니다.")
    private Long optionId;

}