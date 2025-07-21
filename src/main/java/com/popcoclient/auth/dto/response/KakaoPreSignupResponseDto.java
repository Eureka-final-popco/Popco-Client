package com.popcoclient.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class KakaoPreSignupResponseDto {
    private String email;
    private String nickname;

    public static KakaoPreSignupResponseDto of(String email, String nickname) {
        return KakaoPreSignupResponseDto.builder()
                .email(email)
                .nickname(nickname)
                .build();
    }
}
