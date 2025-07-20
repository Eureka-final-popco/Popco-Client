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
public class RefreshResponseDto {
    private String grantType;
    private String accessToken;
    private String refreshToken;

    public static RefreshResponseDto from(JwtToken jwtToken) {
        return RefreshResponseDto.builder()
                .grantType(jwtToken.getGrantType())
                .accessToken(jwtToken.getAccessToken())
                .refreshToken(jwtToken.getRefreshToken())
                .build();
    }
}
