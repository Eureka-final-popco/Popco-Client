package com.popcoclient.auth.controller;

import com.popcoclient.auth.dto.request.RefreshRequestDto;
import com.popcoclient.auth.dto.response.KakaoLoginResponseDto;
import com.popcoclient.auth.dto.response.KakaoPreSignupResponseDto;
import com.popcoclient.auth.dto.response.LoginResponseDto;
import com.popcoclient.auth.dto.response.RefreshResponseDto;
import com.popcoclient.auth.service.AuthService;
import com.popcoclient.common.response.ApiResponse;
import com.popcoclient.auth.dto.request.LoginRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto request) {
        LoginResponseDto response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("login success", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshResponseDto>> refreshToken(@Valid @RequestBody RefreshRequestDto request) {
        RefreshResponseDto response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("refresh token success", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String accessToken) {
        authService.logout(accessToken);
        return ResponseEntity.ok(ApiResponse.success("logout success", null));
    }

    @PostMapping("/kakao/login")
    public ResponseEntity<ApiResponse<Object>> kakaoLogin(@RequestParam("code") String accessCode) {
        KakaoLoginResponseDto response = authService.kakaoLogin(accessCode);

        if (response.isLoginSuccess()) {
            LoginResponseDto loginData = response.getData();
            return ResponseEntity.ok(ApiResponse.success("login success", loginData));
        } else {
            KakaoPreSignupResponseDto signupData = response.getData();
            return ResponseEntity.ok(ApiResponse.success("signup required", signupData));
        }
    }
}
