package com.fondant.infra.jwt.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        JWTErrorCode errorCode = JWTErrorCode.ACCESS_INVALID;

        Object exception = request.getAttribute("exception");

        if (exception instanceof JWTErrorCode) {
            errorCode = (JWTErrorCode) exception;
        }

        JWTResponseUtil.sendErrorResponse(response, errorCode);
    }
}