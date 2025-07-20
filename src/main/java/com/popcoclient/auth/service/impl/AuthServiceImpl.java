package com.popcoclient.auth.service.impl;

import com.popcoclient.auth.dto.request.LoginRequestDto;
import com.popcoclient.auth.dto.request.RefreshRequestDto;
import com.popcoclient.auth.dto.response.LoginResponseDto;
import com.popcoclient.auth.dto.response.RefreshResponseDto;
import com.popcoclient.auth.service.AuthService;
import com.popcoclient.auth.jwt.JwtToken;
import com.popcoclient.auth.jwt.JwtUtil;
import com.popcoclient.exception.business.UserNotFoundException;
import com.popcoclient.user.entity.User;
import com.popcoclient.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        User user = userRepository.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. Email: " + loginRequestDto.getEmail()));

        JwtToken token = jwtUtil.generateToken(user.getUserId());
        return LoginResponseDto.from(token);
    }

    @Override
    public RefreshResponseDto refreshToken(RefreshRequestDto refreshRequestDto) {
        JwtToken token = jwtUtil.refreshAccessToken(refreshRequestDto.getRefreshToken());
        return RefreshResponseDto.from(token);
    }

    @Override
    public void logout(String accessToken) {
        String token = accessToken.replace("Bearer ", "");
        jwtUtil.logout(token);
    }
}
