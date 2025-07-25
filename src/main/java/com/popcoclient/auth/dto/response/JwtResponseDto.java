package com.popcoclient.auth.dto.response;

import com.popcoclient.auth.jwt.JwtToken;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minidev.json.annotate.JsonIgnore;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class JwtResponseDto {
    private String grantType;
    private String accessToken;

    public static JwtResponseDto from(JwtToken jwtToken) {
        return JwtResponseDto.builder()
                .grantType(jwtToken.getGrantType())
                .accessToken(jwtToken.getAccessToken())
                .build();
    }
}
