package com.popcoclient.auth.controller;

import com.popcoclient.auth.dto.request.RefreshRequestDto;
import com.popcoclient.auth.dto.response.KakaoLoginResponseDto;
import com.popcoclient.auth.dto.response.KakaoPreSignupResponseDto;
import com.popcoclient.auth.dto.response.LoginResponseDto;
import com.popcoclient.auth.dto.response.RefreshResponseDto;
import com.popcoclient.auth.service.AuthService;
import com.popcoclient.common.response.ApiResponse;
import com.popcoclient.auth.dto.request.LoginRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증", description = "로그인, 토큰 갱신 등 사용자 인증 관련 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "로그인", description = "사용자 ID와 비밀번호를 사용하여 로그인하고 JWT 토큰을 발급받습니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto request) {
        LoginResponseDto response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login Success", response));
    }

    @Operation(summary = "토큰 갱신", description = "만료된 Access Token과 Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급받습니다.")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshResponseDto>> refreshToken(@Valid @RequestBody RefreshRequestDto request) {
        RefreshResponseDto response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Refresh Token Success", response));
    }

    @Operation(summary = "로그아웃", description = "현재 사용자의 Access Token을 무효화하여 로그아웃 처리합니다.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String accessToken) {
        authService.logout(accessToken);
        return ResponseEntity.ok(ApiResponse.success("Logout Success", null));
    }

    @Operation(summary = "카카오 로그인", description = "카카오 OAuth2 연동을 통해 로그인하거나 회원가입이 필요한 경우 사용자 정보를 반환합니다.")
    @PostMapping("/kakao/login")
    public ResponseEntity<ApiResponse<Object>> kakaoLogin(@RequestParam("code") String accessCode) {
        KakaoLoginResponseDto response = authService.kakaoLogin(accessCode);

        if (response.isLoginSuccess()) {
            LoginResponseDto loginData = response.getData();
            return ResponseEntity.ok(ApiResponse.success("LOGIN", loginData));
        } else {
            KakaoPreSignupResponseDto signupData = response.getData();
            return ResponseEntity.ok(ApiResponse.success("SIGNUP", signupData));
        }
    }
}
