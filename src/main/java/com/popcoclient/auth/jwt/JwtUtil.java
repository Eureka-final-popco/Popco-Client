package com.popcoclient.auth.jwt;

import com.popcoclient.redis.entity.BlackList;
import com.popcoclient.redis.repository.BlackListRepository;
import com.popcoclient.redis.entity.Token;
import com.popcoclient.redis.repository.TokenRepository;
import com.popcoclient.exception.business.auth.InvalidRefreshToken;
import com.popcoclient.exception.business.auth.RefreshTokenMisMatch;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtUtil {

    private static final String GRANT_TYPE = "Bearer";
    private final JwtProvider jwtProvider;
    private final Key key;
    private final TokenRepository tokenRepository;
    private final BlackListRepository blackListRepository;

    @Value("${jwt.access-token.expire-time}")
    private int ACCESS_TOKEN_EXPIRE_TIME;

    @Value("${jwt.refresh-token.expire-time}")
    private int REFRESH_TOKEN_EXPIRE_TIME;

    @Value("${jwt.threshold-time}")
    private int THRESHOLD_TIME;

    public JwtUtil(@Value("${jwt.secret}") String secretKey,
                   TokenRepository tokenRepository, JwtProvider jwtProvider, BlackListRepository blackListRepository) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes); // SecretKey 객체 생성
        this.tokenRepository = tokenRepository;
        this.jwtProvider = jwtProvider;
        this.blackListRepository = blackListRepository;
    }

    // Member 정보를 가지고 AccessToken, RefreshToken을 생성하기
    public JwtToken generateToken(Long userId) {
        Optional<Token> existingRefreshToken = tokenRepository.findById(userId);
        long now = (new Date()).getTime();

        String refreshToken;
        String role = "ROLE_USER";

        if (existingRefreshToken.isPresent()) {
            String existingToken = existingRefreshToken.get().getRefreshToken();
            boolean needNewRefreshToken = isRefreshTokenExpiringSoon(existingToken);
            if (!needNewRefreshToken) {
                refreshToken = existingToken;
            } else {
                refreshToken = generateRefreshToken(userId, new Date(now + REFRESH_TOKEN_EXPIRE_TIME));
                tokenRepository.deleteById(userId);
                tokenRepository.save(new Token(userId, refreshToken));
            }
        } else {
            refreshToken = generateRefreshToken(userId, new Date(now + REFRESH_TOKEN_EXPIRE_TIME));
            tokenRepository.save(new Token(userId, refreshToken));
        }

        // AccessToken 생성
        Date accessTokenExpire = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
        String accessToken = generateAccessToken(userId, role, accessTokenExpire);

        return JwtToken.builder()
                .grantType(GRANT_TYPE)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public JwtToken refreshAccessToken(String refreshToken) {
        // 1. RefreshToken 유효성 검사
        if (!jwtProvider.validateRefreshToken(refreshToken)) {
            throw new InvalidRefreshToken();
        }

        // 2. RefreshToken에서 userId 추출
        Long userId = Long.valueOf(jwtProvider.getUserIdFromToken(refreshToken));

        // 3. Redis에 저장된 RefreshToken 조회 및 일치 여부 확인
        Optional<Token> existingRefreshToken = tokenRepository.findById(userId);
        if (existingRefreshToken.isPresent()) {
            String storedRefreshToken = existingRefreshToken.get().getRefreshToken();
            if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
                throw new RefreshTokenMisMatch();
            }
        }

        long now = (new Date()).getTime();
        boolean needNewRefreshToken = isRefreshTokenExpiringSoon(refreshToken);

        String newRefreshToken = refreshToken;
        if (needNewRefreshToken) {
            // 새 RefreshToken 생성 및 Redis에 저장
            newRefreshToken = generateRefreshToken(userId, new Date(now + REFRESH_TOKEN_EXPIRE_TIME));
            tokenRepository.deleteById(userId);
            tokenRepository.save(new Token(userId, newRefreshToken));
        }

        // 5. 새 AccessToken 생성
        Date accessTokenExpire = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
        String newAccessToken = generateAccessToken(userId, "ROLE_USER", accessTokenExpire);

        // 6. JwtToken 객체로 반환
        return JwtToken.builder()
                .grantType(GRANT_TYPE)
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    public void logout(String token) {
        blackListRepository.save(new BlackList(token));

        Long userId = Long.valueOf(jwtProvider.getUserIdFromToken(token));
        tokenRepository.deleteById(userId);
    }

    private boolean isRefreshTokenExpiringSoon(String refreshToken) {
        long now = (new Date()).getTime();
        Claims claims = jwtProvider.parseClaims(refreshToken);
        long expirationTime = claims.getExpiration().getTime();
        return (expirationTime - now) < THRESHOLD_TIME;
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

    public Cookie setRefreshTokenCookie(String refreshToken) {
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);

        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(REFRESH_TOKEN_EXPIRE_TIME);
        refreshTokenCookie.setAttribute("SameSite", "Lax");
        refreshTokenCookie.setSecure(false);

        return refreshTokenCookie;
    }
}