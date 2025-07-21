package com.popcoclient.auth.filter;

import com.popcoclient.auth.jwt.JwtProvider;
import com.popcoclient.exception.business.auth.TokenExpiredException;
import com.popcoclient.redis.repository.BlackListRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends GenericFilterBean {
    private final JwtProvider jwtTokenProvider;
    private final BlackListRepository blackListRepository;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse response = (HttpServletResponse) servletResponse;

        try {
            // 1. Request Header에서 JWT 토큰 추출
            String token = resolveToken((HttpServletRequest) servletRequest);
            // 2. validateToken으로 토큰 유효성 검사
            if (token != null){
                if (jwtTokenProvider.validateToken(token) && !blackListRepository.existsById(token)){
                    Authentication authentication = jwtTokenProvider.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            }
        } catch (TokenExpiredException e) {
            logger.error(e.getMessage(), e);
            response.setStatus(e.getErrorCode().getStatus().value());
            response.setContentType("application/json");
            response.getWriter().write(
                    String.format("{\"code\":\"%s\"}",
                            e.getErrorCode().getCode()));
            return;
        }

        chain.doFilter(servletRequest, servletResponse);
    }

    // Request Header에서 토큰 정보 추출
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer")) {
            return bearerToken.substring(7);
        }
        return null;
    }

}