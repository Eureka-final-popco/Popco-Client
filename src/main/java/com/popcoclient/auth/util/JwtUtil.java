package com.popcoclient.auth.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String GRANT_TYPE = "Bearer";
    private final Key key;

    @Value("${jwt.access-token.expire-time}")
    private long ACCESS_TOKEN_EXPIRE_TIME;

    @Value("${jwt.refresh-token.expire-time}")
    private long REFRESH_TOKEN_EXPIRE_TIME;

    public JwtUtil(@Value("${jwt.secret}") String secretKey ) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes); // SecretKey 객체 생성
    }

    // Member 정보를 가지고 AccessToken, RefreshToken을 생성하기
    public JwtToken generateToken(Long userId) {
        long now = (new Date()).getTime();
        String role = "ROLE_USER";

        // AccessToken 생성
        Date accessTokenExpire = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
        String accessToken = generateAccessToken(userId, role, accessTokenExpire);

        // RefreshToken 생성
        Date refreshTokenExpire = new Date(now + REFRESH_TOKEN_EXPIRE_TIME);
        String refreshToken = generateRefreshToken(userId, refreshTokenExpire);

        // Redis에 RefreshToken 넣기
        // "REFRESH_TOKEN_EXPIRE_TIME"만큼 시간이 지나면 삭제됨
//        redisDao.setValues(username, refreshToken, Duration.ofMillis(REFRESH_TOKEN_EXPIRE_TIME));

        return JwtToken.builder()
                .grantType(GRANT_TYPE)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // AccessToken & RefreshToken 재발급 할 때는 비밀번호가 필요 없음
    // 이미 유효한 RefreshToken을 가지고 있다는 것이 인증된 사용자이므로
    private String generateAccessToken(Long userId, String authorities, Date expireDate) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId)) // 토큰 제목 (사용자 이름)
                .claim("auth", authorities)
                .setExpiration(expireDate) // 토큰 만료 시간
                .signWith(key, SignatureAlgorithm.HS256) // 지정된 키와 알고리즘으로 서명
                .compact(); // 최종 JWT 문자열 생성 (header.payload.signature 구조);
    }

    private String generateRefreshToken(Long userId, Date expireDate) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setExpiration(expireDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

}