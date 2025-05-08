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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
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
            throw new ApiException(UserError.REFRESH_INVALID);
        }

        if (!refreshRepository.existsByRefresh(refresh)) {
            throw new ApiException(UserError.REFRESH_NOT_FOUND);
        }

        Long userId = jwtUtil.getUserIdFromToken(refresh);
        String role = jwtUtil.getUserRoleFromToken(refresh);

        String newAccess = jwtUtil.generateToken("access", userId, role, 60 * 10 * 1000L);
        String newRefresh = jwtUtil.generateToken("refresh", userId, role, 60 * 60 * 24 * 1000L);

        refreshRepository.deleteByRefresh(refresh);
        addRefreshEntity(userId, newRefresh, 60 * 60 * 24 * 1000L);

        Map<String, Object> responseBody = new HashMap<>();
        Map<String, Object> innerResponse = new HashMap<>();

        innerResponse.put("accessToken", newAccess);

        responseBody.put("code", SUCCESS);
        responseBody.put("message", "요청이 성공적으로 처리되었습니다.");
        responseBody.put("response", innerResponse);

        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();
        writer.print(new ObjectMapper().writeValueAsString(responseBody));

        response.addCookie(createCookie("refresh", newRefresh));

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new ApiException(UserError.REFRESH_NOT_FOUND);
        }
        for (Cookie cookie : cookies) {
            if ("refresh".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        throw new ApiException(UserError.REFRESH_NOT_FOUND);
    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60 * 60 * 24);
        //cookie.setSecure(true); Https 사용시
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        return cookie;
    }

    private void addRefreshEntity(Long userId, String refresh, Long expiredMs) {
        LocalDateTime date = LocalDateTime.now().plusSeconds(expiredMs).atZone(ZoneId.systemDefault()).toLocalDateTime();

        RefreshEntity refreshEntity = RefreshEntity.builder()
                .userId(userId)
                .refresh(refresh)
                .expires(date)
                .build();

        refreshRepository.save(refreshEntity);
    }
}