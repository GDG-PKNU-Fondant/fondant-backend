package com.fondant.user.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fondant.global.exception.ApiException;
import com.fondant.infra.jwt.application.JWTUtil;
import com.fondant.infra.jwt.domain.entity.RefreshEntity;
import com.fondant.infra.jwt.domain.repository.RefreshRepository;
import com.fondant.infra.jwt.exception.JWTErrorCode;
import com.fondant.user.exception.UserError;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

import static org.springframework.scheduling.config.TaskExecutionOutcome.Status.SUCCESS;

@Service
public class ReissueService {

    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    public ReissueService(JWTUtil jwtUtil, RefreshRepository refreshRepository) {
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
    }

    public ResponseEntity<?> reissueToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String refresh = extractRefreshTokenFromCookie(request);

        try {
            jwtUtil.isTokenExpired(refresh);
        } catch (ExpiredJwtException e) {
            throw new ApiException(UserError.REFRESH_EXPIRED);
        }

        String type = jwtUtil.getType(refresh);

        if (type == null || !type.equals("refresh")) {
            System.out.println("not a refresh token:" + refresh);
            throw new ApiException(UserError.REFRESH_INVALID);
        }
        System.out.println(refresh);

        if (!refreshRepository.existsByRefresh(refresh)) {
            System.out.println("cannot find refresh token:" + refresh);
            throw new ApiException(UserError.REFRESH_INVALID);
        }

        Long userId = jwtUtil.getUserIdFromToken(refresh);
        String role = jwtUtil.getUserRoleFromToken(refresh);

        String newAccess = jwtUtil.generateToken("access", userId, role, 60 * 10 * 1000L);
        String newRefresh = jwtUtil.generateToken("refresh", userId, role, 60 * 60 * 24 * 1000L);

        refreshRepository.deleteByRefresh(refresh);

        addRefreshEntity(userId, newRefresh);

        ResponseCookie cookie = ResponseCookie.from("refresh", newRefresh)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ofDays(1))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        Map<String, Object> body = Map.of(
                "code", SUCCESS,
                "message", "요청이 성공적으로 처리되었습니다.",
                "content", Map.of("accessToken", newAccess)
        );
        return ResponseEntity.ok(body);
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            System.out.println("cookies is null:" + Arrays.toString(cookies));
            throw new ApiException(UserError.REFRESH_INVALID);
        }
        for (Cookie cookie : cookies) {
            if ("refresh".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        System.out.println("refresh is null" + Arrays.toString(cookies));
        throw new ApiException(UserError.REFRESH_INVALID);
    }

    private void addRefreshEntity(Long userId, String refresh) {
        LocalDateTime date = jwtUtil.getExpiration(refresh);

        RefreshEntity refreshEntity = RefreshEntity.builder()
                .userId(userId)
                .refresh(refresh)
                .expires(date)
                .build();

        refreshRepository.save(refreshEntity);
    }
}