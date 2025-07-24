package com.popcoclient.auth.service;

import com.popcoclient.auth.dto.request.LoginRequestDto;
import com.popcoclient.auth.dto.request.RefreshRequestDto;
import com.popcoclient.auth.dto.response.KakaoLoginResponseDto;
import com.popcoclient.auth.dto.response.LoginResponseDto;
import com.popcoclient.auth.dto.response.RefreshResponseDto;
import com.popcoclient.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;

public interface AuthService {
    ApiResponse<LoginResponseDto> login(LoginRequestDto loginRequestDto, HttpServletResponse response);
    ApiResponse<RefreshResponseDto> refreshToken(String refreshTokenHeader, HttpServletResponse response);
    void logout(String accessToken);
    KakaoLoginResponseDto kakaoLogin(String accessCode, HttpServletResponse response);
}
