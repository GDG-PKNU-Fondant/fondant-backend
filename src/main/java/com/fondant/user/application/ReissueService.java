package com.fondant.user.application;

import com.fondant.user.jwt.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.PrintWriter;

@Service
public class ReissueService {

    private final JWTUtil jwtUtil;

    public ReissueService(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
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

        String userId = jwtUtil.getUserIdFromToken(refresh);
        String role = jwtUtil.getUserRoleFromToken(refresh);

        String newAccess = jwtUtil.generateToken("access", userId, role, 60 * 10L);
        String newRefresh = jwtUtil.generateToken("refresh", userId, role, 60 * 60 * 24L);

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
}
