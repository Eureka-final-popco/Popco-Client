package com.popcoclient.auth.service;

import com.popcoclient.auth.dto.request.LoginRequestDto;
import com.popcoclient.auth.dto.request.RefreshRequestDto;
import com.popcoclient.auth.dto.response.KakaoLoginResponseDto;
import com.popcoclient.auth.dto.response.LoginResponseDto;
import com.popcoclient.auth.dto.response.RefreshResponseDto;

public interface AuthService {
    LoginResponseDto login(LoginRequestDto loginRequestDto);
    RefreshResponseDto refreshToken(RefreshRequestDto refreshRequestDto);
    void logout(String accessToken);
    KakaoLoginResponseDto kakaoLogin(String accessCode);
}
