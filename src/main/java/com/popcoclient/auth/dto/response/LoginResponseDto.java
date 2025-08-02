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
    private Long userId;
    private LoginUserResponseDto loginUserResponseDto;
    private JwtResponseDto jwtResponseDto;
    private boolean isProfileComplete;


    public static LoginResponseDto of(Long userId,
            LoginUserResponseDto loginUserResponseDto, JwtResponseDto jwtResponse, boolean isProfileComplete) {
        return LoginResponseDto.builder()
                .userId(userId)
                .loginUserResponseDto(loginUserResponseDto)
                .jwtResponseDto(jwtResponse)
                .isProfileComplete(isProfileComplete)
                .build();
    }
}
