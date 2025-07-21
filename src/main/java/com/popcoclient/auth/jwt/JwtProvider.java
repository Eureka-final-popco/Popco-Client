package com.popcoclient.auth.jwt;

import com.popcoclient.redis.entity.Token;
import com.popcoclient.redis.repository.TokenRepository;
import com.popcoclient.exception.business.auth.UnauthorizedUserException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.beans.factory.annotation.Value;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

@Component
@Slf4j
public class JwtProvider {
    private final Key key;
    private final TokenRepository tokenRepository;

    public JwtProvider(@Value("${jwt.secret}") String secretKey, TokenRepository tokenRepository) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.tokenRepository = tokenRepository;
    }

    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);
        String subject = claims.getSubject();
        String roles = claims.get("auth", String.class);

        Collection<? extends GrantedAuthority> authorities = Arrays.stream(roles.split(","))
                .map(SimpleGrantedAuthority::new)
                .toList();

        UserDetails principal = new User(subject, "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    public Long getUserIdFromAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedUserException();
        }

        String principal = authentication.getName();
        return Long.parseLong(principal);
    }

    // 토큰 정보를 검증하는 메서드
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token", e);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token", e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty.", e);
        }
        return false;
    }

    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    public boolean validateRefreshToken(String token) {
        // 기본적인 JWT 검증
        if (!validateToken(token)) return false;
        String redisToken = "";

        try {
            // token에서 userId 추출하기
            Long userId = Long.valueOf(getUserIdFromToken(token));
            // Redis에 저장된 RefreshToken과 비교하기
            Optional<Token> validateToken = tokenRepository.findById(userId);
            if (validateToken.isPresent()) {
                redisToken = validateToken.get().getRefreshToken();
            }
            return token.equals(redisToken);
        } catch (Exception e) {
            log.info("RefreshToken Validation Failed", e);
            return false;
        }
    }

    public String getUserIdFromToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            // 토큰이 만료되어도 클레임 내용을 가져올 수 있음
            return e.getClaims().getSubject();
        }
    }

}
