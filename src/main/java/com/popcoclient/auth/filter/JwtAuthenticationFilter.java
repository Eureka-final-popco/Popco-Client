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
                    // 블랙리스트 된 토큰일 경우 401 응답 후 종료
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"code\":\"BLACKLISTED_TOKEN\"}");
                    return;
                }
            } else if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken, "REFRESH")) {
                // 리프레시 토큰 유효하지만 액세스 토큰 만료된 경우 - 재발급 필요 표시
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"code\":\"ACCESS_TOKEN_EXPIRED\"}");
                return;
            }
            // 토큰 없거나 유효하지 않아도 인증 정보 없이 그냥 다음 필터/컨트롤러로 진행
            chain.doFilter(request, response);

        } catch (TokenExpiredException e) {
            logger.error(e.getMessage(), e);
            response.setStatus(e.getErrorCode().getStatus().value());
            response.setContentType("application/json");
            response.getWriter().write(
                    String.format("{\"code\":\"%s\"}", e.getErrorCode().getCode()));
        }
    }

    // Request Header에서 토큰 정보 추출
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