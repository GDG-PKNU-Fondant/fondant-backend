package com.fondant.user.application;

import com.fondant.infra.jwt.application.JWTUtil;
import com.fondant.infra.jwt.domain.entity.RefreshEntity;
import com.fondant.infra.jwt.domain.repository.RefreshRepository;
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

@Service
public class ReissueService {

    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    public ReissueService(JWTUtil jwtUtil, RefreshRepository refreshRepository) {
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
    }

    public ResponseEntity<?> reissueToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String refresh = null;
        Cookie[] cookies = request.getCookies();

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refresh")) {
                refresh = cookie.getValue();
            }
        }

        if (refresh == null) {
            return new ResponseEntity<>("리프레쉬 토큰이 존재하지 않습니다.", HttpStatus.BAD_REQUEST);
        }

        try {
            jwtUtil.isTokenExpired(refresh);
        } catch (ExpiredJwtException e) {
            return new ResponseEntity<>("리프레쉬 토큰이 만료되었습니다.", HttpStatus.BAD_REQUEST);
        }

        String type = jwtUtil.getType(refresh);

        if (!type.equals("refresh")) {
            return new ResponseEntity<>("유효하지 않은 토큰입니다.", HttpStatus.BAD_REQUEST);
        }

        if (!refreshRepository.existsByRefresh(refresh)){
            return new ResponseEntity<>("존재하지 않는 토큰입니다.", HttpStatus.BAD_REQUEST);
        }

        String userId = jwtUtil.getUserIdFromToken(refresh);
        String role = jwtUtil.getUserRoleFromToken(refresh);

        String newAccess = jwtUtil.generateToken("access", userId, role, 60 * 10L);
        String newRefresh = jwtUtil.generateToken("refresh", userId, role, 60 * 60 * 24L);

        refreshRepository.deleteByRefresh(refresh);
        addRefreshEntity(userId, newRefresh, 60 * 60 * 24L);

        PrintWriter writer = response.getWriter();
        writer.print("access: " + newAccess);
        response.addCookie(createCookie("refresh", newRefresh));

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60 * 60 * 24);
        //cookie.setSecure(true); Https 사용시
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        return cookie;
    }

    private void addRefreshEntity(String userId, String refresh, Long expiredMs) {
        LocalDateTime date = LocalDateTime.now().plusSeconds(expiredMs).atZone(ZoneId.systemDefault()).toLocalDateTime();

        RefreshEntity refreshEntity = RefreshEntity.builder()
                .userId(userId)
                .refresh(refresh)
                .expires(date)
                .build();

        refreshRepository.save(refreshEntity);
    }
}