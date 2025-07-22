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
    private UserResponseDto userResponseDto;
    private JwtResponseDto jwtResponseDto;
    private boolean isProfileComplete;

    public static LoginResponseDto of(
            UserResponseDto userResponse, JwtResponseDto jwtResponse, boolean isProfileComplete) {
        return LoginResponseDto.builder()
                .userResponseDto(userResponse)
                .jwtResponseDto(jwtResponse)
                .isProfileComplete(isProfileComplete)
                .build();
    }
}
