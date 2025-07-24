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

        JwtToken token = jwtUtil.generateToken(user.getUserId());

        Cookie cookie = jwtUtil.setRefreshTokenCookie(token.getRefreshToken());
        LoginResponseDto responseDto = buildLoginResponse(user, token);

        response.addCookie(cookie);

        return ApiResponse.success("LOGIN", responseDto);
    }

    @Override
    public ApiResponse<RefreshResponseDto> refreshToken(String refreshTokenHeader, HttpServletResponse response) {
        JwtToken token = jwtUtil.refreshAccessToken(refreshTokenHeader);

        Cookie cookie = jwtUtil.setRefreshTokenCookie(token.getRefreshToken());

        response.addCookie(cookie);

        return ApiResponse.success("LOGIN", RefreshResponseDto.from(token));
    }

    @Override
    public void logout(String accessToken) {
        String token = accessToken.replace("Bearer ", "");
        jwtUtil.logout(token);
    }

    @Override
    public KakaoLoginResponseDto kakaoLogin(String accessCode, HttpServletResponse response) {
        KakaoToken token = kakaoUtil.requestToken(accessCode);
        KakaoProfile kakaoProfile = kakaoUtil.requestProfile(token);

        String email = kakaoProfile.getKakao_account().getEmail();
        String nickname = kakaoProfile.getKakao_account().getProfile().getNickname();

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            // ✅ 로그인 완료 → JWT 발급
            JwtToken jwt = jwtUtil.generateToken(userOpt.get().getUserId());

            Cookie cookie = jwtUtil.setRefreshTokenCookie(jwt.getRefreshToken());
            response.addCookie(cookie);

            LoginResponseDto loginResponse = buildLoginResponse(userOpt.get(), jwt);
            return KakaoLoginResponseDto.loginSuccess(ApiResponse.success("LOGIN", loginResponse));
        } else {
            // ❗아직 회원가입 안됨 → 추가 정보 필요
            KakaoPreSignupResponseDto preSignup = KakaoPreSignupResponseDto.of(email,nickname);
            return KakaoLoginResponseDto.signupRequired(ApiResponse.success("SIGNUP", preSignup));
        }
    }

    private LoginResponseDto buildLoginResponse(User user, JwtToken token) {
                Optional<UserDetail> userDetailOpt = userDetailRepository.findById(user.getUserId());
        LoginUserResponseDto userResponseDto = null;
        boolean isProfileComplete = false;

        if (userDetailOpt.isPresent()) {
            userResponseDto = LoginUserResponseDto.from(userDetailOpt.get());
            isProfileComplete = true;
        }

        return LoginResponseDto.of(userResponseDto, JwtResponseDto.from(token), isProfileComplete);
    }

}
