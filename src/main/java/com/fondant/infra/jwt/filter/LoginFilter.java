package com.fondant.infra.jwt.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fondant.infra.jwt.application.JWTUtil;
import com.fondant.infra.jwt.domain.entity.RefreshEntity;
import com.fondant.infra.jwt.domain.repository.RefreshRepository;
import com.fondant.user.application.dto.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private RefreshRepository refreshRepository;

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil, RefreshRepository refreshRepository) {
        super.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/api/user/login", "POST"));
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String userEmail;
        String password;

        try {
            Map jsonRequest = objectMapper.readValue(request.getInputStream(), Map.class);
            userEmail = (String) jsonRequest.get("email");
            password = (String) jsonRequest.get("password");

            if (userEmail == null || password == null) {
                throw new AuthenticationException("메일 또는 비밀번호가 입력되지 않았습니다.") {
                };
            }

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userEmail, password);
            return authenticationManager.authenticate(authToken);
        } catch (IOException e) {
            throw new AuthenticationServiceException("JSON 요청을 읽는 중 오류가 발생했습니다.", e) {
            };
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException {
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        String userId = customUserDetails.getUserId();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();

        String role = auth.getAuthority();

        String accessToken = jwtUtil.generateToken("access", userId, role, 60 * 10 * 1000L);
        String refreshToken = jwtUtil.generateToken("refresh", userId, role, 60 * 60 * 24 * 1000L);

        addRefreshEntity(userId, refreshToken);

        PrintWriter writer = response.getWriter();
        writer.print("access: " + accessToken);
        response.addCookie(createCookie("refresh", refreshToken));
        response.setStatus(HttpStatus.OK.value());
    }

    private void addRefreshEntity(String userId, String refresh) {
        LocalDateTime date = jwtUtil.getExpiration(refresh);

        RefreshEntity refreshEntity = RefreshEntity.builder()
                .userId(userId)
                .refresh(refresh)
                .expires(date)
                .build();

        refreshRepository.save(refreshEntity);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed){
        response.setStatus(401);
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
