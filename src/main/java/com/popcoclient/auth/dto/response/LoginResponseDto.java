package com.popcoclient.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class LoginResponseDto {
    private LoginUserResponseDto loginUserResponseDto;
    private JwtResponseDto jwtResponseDto;
    private boolean isProfileComplete;

    public static LoginResponseDto of(
            LoginUserResponseDto userResponse, JwtResponseDto jwtResponse, boolean isProfileComplete) {
        return LoginResponseDto.builder()
                .loginUserResponseDto(userResponse)
                .jwtResponseDto(jwtResponse)
                .isProfileComplete(isProfileComplete)
                .build();
    }
}
