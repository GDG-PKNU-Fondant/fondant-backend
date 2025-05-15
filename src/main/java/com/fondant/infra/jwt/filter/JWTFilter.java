package com.fondant.infra.jwt.filter;

import com.fondant.infra.jwt.application.JWTUtil;
import com.fondant.infra.jwt.exception.JWTErrorCode;
import com.fondant.infra.jwt.exception.JWTResponseUtil;
import com.fondant.user.application.dto.CustomUserDetails;
import com.fondant.infra.jwt.dto.JWTUserDTO;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final String[] excludeUrls;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JWTFilter(JWTUtil jwtUtil, String[] excludeUrls) {
        this.jwtUtil = jwtUtil;
        this.excludeUrls = excludeUrls;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        System.out.println(Arrays.toString(excludeUrls));
        System.out.println(request.getRequestURI());
        String path = request.getRequestURI();
        return Arrays.stream(excludeUrls)
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            if (!shouldNotFilter(request)) {
                logger.error("ACCESS TOKEN이 존재하지 않습니다.");
                JWTResponseUtil.sendErrorResponse(response, JWTErrorCode.ACCESS_INVALID);
                return;
            }
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = token.substring(7);

        try {
            jwtUtil.isTokenExpired(accessToken);
            String type = jwtUtil.getType(accessToken);

            if (!"access".equals(type)) {
                logger.error("ACCESS TOKEN이 아닙니다.");
                JWTResponseUtil.sendErrorResponse(response, JWTErrorCode.ACCESS_INVALID);
                return;
            }

            Long userId = jwtUtil.getUserIdFromToken(accessToken);
            String role = jwtUtil.getUserRoleFromToken(accessToken);

            JWTUserDTO user = JWTUserDTO.builder()
                    .userId(userId)
                    .role(role)
                    .build();

            CustomUserDetails customUserDetails = new CustomUserDetails(user);

            Authentication authToken = new UsernamePasswordAuthenticationToken(
                    customUserDetails, null, customUserDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            logger.error("ACCESS TOKEN이 만료되었습니다.", e);
            JWTResponseUtil.sendErrorResponse(response, JWTErrorCode.ACCESS_EXPIRED);
        } catch (IllegalArgumentException e) {
            logger.error("ACCESS TOKEN의 인자가 잘못되었습니다.", e);
            JWTResponseUtil.sendErrorResponse(response, JWTErrorCode.ACCESS_INVALID);
        } catch (Exception e) {
            logger.error("ACCESS TOKEN 처리 중 오류가 발생했습니다.", e);
            JWTResponseUtil.sendErrorResponse(response, JWTErrorCode.ACCESS_INVALID);
        }
    }
}