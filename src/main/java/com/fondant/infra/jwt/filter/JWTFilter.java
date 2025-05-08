package com.fondant.infra.jwt.filter;

import com.fondant.infra.jwt.application.JWTUtil;
import com.fondant.infra.jwt.exception.JWTErrorCode;
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
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = token.substring(7);

        try {
            jwtUtil.isTokenExpired(accessToken);
            String type = jwtUtil.getType(accessToken);

            if (!"access".equals(type)) {
                throw new IllegalArgumentException("Invalid token type");
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
            request.setAttribute("exception", JWTErrorCode.EXPIRED);
            filterChain.doFilter(request, response);
        } catch (IllegalArgumentException e) {
            request.setAttribute("exception", JWTErrorCode.INVALID_TYPE);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            request.setAttribute("exception", JWTErrorCode.INVALID);
            filterChain.doFilter(request, response);
        }
    }
}