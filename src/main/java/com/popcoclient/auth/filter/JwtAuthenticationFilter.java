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

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String requestURI = ((HttpServletRequest) servletRequest).getRequestURI();

        try {
            String accessToken = resolveAccessToken(request);
            String refreshToken = resolveRefreshToken(request);

            if (accessToken != null && jwtTokenProvider.validateToken(accessToken, "ACCESS")) {
                if (!blackListRepository.existsById(accessToken)) {
                    Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"code\":\"BLACKLISTED_TOKEN\"}");
                    return;
                }
            } else if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken, "REFRESH")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"code\":\"ACCESS_TOKEN_EXPIRED\"}");
                return;
            }

            chain.doFilter(request, response);

        } catch (TokenExpiredException e) {
            logger.error(e.getMessage(), e);
            response.setStatus(e.getErrorCode().getStatus().value());
            response.setContentType("application/json");
            response.getWriter().write(
                    String.format("{\"code\":\"%s\"}", e.getErrorCode().getCode()));
        }
    }

    private String resolveAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private String resolveRefreshToken(HttpServletRequest request) {
        return request.getHeader("X-Refresh-Token");  // 커스텀 헤더에서 그대로 추출
    }

}