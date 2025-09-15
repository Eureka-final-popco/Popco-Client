package com.popcoclient.auth.service.impl;

import com.popcoclient.auth.dto.request.LoginRequestDto;
import com.popcoclient.auth.dto.request.RefreshRequestDto;
import com.popcoclient.auth.dto.response.*;
import com.popcoclient.auth.kakao.KakaoProfile;
import com.popcoclient.auth.kakao.KakaoToken;
import com.popcoclient.auth.kakao.KakaoUtil;
import com.popcoclient.auth.service.AuthService;
import com.popcoclient.auth.jwt.JwtToken;
import com.popcoclient.auth.jwt.JwtUtil;
import com.popcoclient.common.response.ApiResponse;
import com.popcoclient.exception.business.InvalidPasswordException;
import com.popcoclient.exception.business.UserNotFoundException;
import com.popcoclient.user.entity.User;
import com.popcoclient.user.entity.UserDetail;
import com.popcoclient.user.repository.UserDetailRepository;
import com.popcoclient.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final UserDetailRepository userDetailRepository;
    private final JwtUtil jwtUtil;
    private final KakaoUtil kakaoUtil;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public ApiResponse<LoginResponseDto> login(LoginRequestDto loginRequestDto, HttpServletResponse response) {
        User user = userRepository.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. Email: " + loginRequestDto.getEmail()));

        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException("비밀번호가 올바르지 않습니다.");
        }

        return ApiResponse.success("LOGIN", createLoginResponseWithCookie(user, response));
    }

    @Override
    public ApiResponse<RefreshResponseDto> refreshToken(String refreshTokenHeader, HttpServletResponse response) {
        JwtToken token = jwtUtil.refreshAccessToken(refreshTokenHeader);

        Cookie cookie = jwtUtil.setRefreshTokenCookie(token.getRefreshToken());

        response.addCookie(cookie);

        return ApiResponse.success("REFRESH TOKEN", RefreshResponseDto.from(token));
    }

    @Override
    public void logout(String accessToken) {
        String token = accessToken.replace("Bearer ", "");
        jwtUtil.logout(token);
    }

    @Override
    public ApiResponse<LoginResponseDto> kakaoLogin(String KakaoAccessCode, HttpServletResponse response) {
        KakaoToken token = kakaoUtil.requestToken(KakaoAccessCode);
        KakaoProfile kakaoProfile = kakaoUtil.requestProfile(token);

        if (kakaoProfile == null || kakaoProfile.getKakao_account() == null) {
            throw new IllegalStateException("카카오 계정 정보가 없습니다.");
        }

        KakaoProfile.KakaoAccount account = kakaoProfile.getKakao_account();

        String email = account.getEmail();
        if (email == null || email.isBlank()) {
            throw new IllegalStateException("이메일 정보가 없습니다.");
        }

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            LoginResponseDto loginResponse = createLoginResponseWithCookie(userOpt.get(), response);
            return ApiResponse.success("LOGIN", loginResponse);
        } else {
            User user = saveNewUser(email);
            LoginResponseDto firstLoginResponse = createLoginResponseWithCookie(user, response);
            return ApiResponse.success("SIGNUP", firstLoginResponse);
        }
    }

    private User saveNewUser(String email) {
        User user = User.of(email, null);
        return userRepository.save(user);
    }

    private LoginResponseDto createLoginResponseWithCookie(User user, HttpServletResponse response) {
        JwtToken jwt = jwtUtil.generateToken(user.getUserId());
        Cookie cookie = jwtUtil.setRefreshTokenCookie(jwt.getRefreshToken());
        response.addCookie(cookie);
        return buildLoginResponse(user, jwt);
    }

    private LoginResponseDto buildLoginResponse(User user, JwtToken token) {
        Optional<UserDetail> userDetailOpt = userDetailRepository.findById(user.getUserId());
        LoginUserResponseDto userResponseDto = null;
        boolean isProfileComplete = false;

        if (userDetailOpt.isPresent()) {
            userResponseDto = LoginUserResponseDto.from(userDetailOpt.get());
            isProfileComplete = true;
        }

        return LoginResponseDto.of(user.getUserId(), userResponseDto, JwtResponseDto.from(token), isProfileComplete);
    }

}
