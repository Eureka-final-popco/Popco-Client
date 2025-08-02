package com.popcoclient.auth.controller;

import com.popcoclient.auth.dto.response.LoginResponseDto;
import com.popcoclient.auth.dto.response.RefreshResponseDto;
import com.popcoclient.auth.service.AuthService;
import com.popcoclient.common.response.ApiResponse;
import com.popcoclient.auth.dto.request.LoginRequestDto;
import com.popcoclient.exception.business.auth.RefreshTokenNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
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

    @Operation(summary = "로그인", description = "사용자 이메일과 비밀번호를 사용하여 로그인하고 JWT 토큰을 발급받습니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(
            @Valid @RequestBody LoginRequestDto request, HttpServletResponse response) {
        ApiResponse<LoginResponseDto> loginResponseDto = authService.login(request, response);

        return ResponseEntity.ok().body(loginResponseDto);
    }

    @Operation(
            summary = "토큰 갱신",
            description = "HttpOnly 쿠키에 저장된 Refresh Token을 사용하여 새로운 Access Token을 발급받습니다.\n\n" +
                    "브라우저는 쿠키를 자동으로 전송하므로, 별도의 헤더 설정 없이도 요청 가능합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshResponseDto>> refreshToken(
            @CookieValue(value = "refreshToken", required = false) String refreshTokenCookie,
            HttpServletResponse response) {

        if (refreshTokenCookie == null) {
            throw new RefreshTokenNotFoundException("Refresh token not found in cookies.");
        }

        ApiResponse<RefreshResponseDto> refreshToken = authService.refreshToken(refreshTokenCookie, response);
        return ResponseEntity.ok(refreshToken);
    }


    @Operation(summary = "로그아웃", description = "현재 사용자의 Access Token을 무효화하여 로그아웃 처리합니다.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String accessToken) {
        authService.logout(accessToken);
        return ResponseEntity.ok(ApiResponse.success("Logout Success", null));
    }

    @Operation(summary = "카카오 로그인", description = "카카오 OAuth2 연동을 통해 로그인하거나 회원가입이 필요한 경우 사용자 정보를 반환합니다.")
    @PostMapping("/kakao/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> kakaoLogin(
            @RequestParam("code") String accessCode, HttpServletResponse response) {
        ApiResponse<LoginResponseDto> kakaoLogin = authService.kakaoLogin(accessCode, response);

        return ResponseEntity.ok(kakaoLogin);
    }
}
