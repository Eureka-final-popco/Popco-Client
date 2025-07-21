package com.popcoclient.auth.dto.response;

import com.popcoclient.auth.jwt.JwtToken;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class LoginResponseDto {
    private String grantType;
    private String accessToken;
    private String refreshToken;

    public static LoginResponseDto from(JwtToken jwtToken) {
        return LoginResponseDto.builder()
                .grantType(jwtToken.getGrantType())
                .accessToken(jwtToken.getAccessToken())
                .refreshToken(jwtToken.getRefreshToken())
                .build();
    }
}
