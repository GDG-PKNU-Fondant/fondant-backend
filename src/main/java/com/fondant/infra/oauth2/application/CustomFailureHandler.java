package com.fondant.infra.oauth2.application;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.io.PrintWriter;

@Component
public class CustomFailureHandler implements AuthenticationFailureHandler {

    @Value("${spring.cors.allowed_origins}")
    private String allowedOrigins;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        String errorMessage;

        if (exception instanceof org.springframework.security.oauth2.core.OAuth2AuthenticationException oauthEx) {
            errorMessage = oauthEx.getError().getDescription();
        } else {
            errorMessage = exception.getMessage();
        }

        String script = "<!DOCTYPE html><html><body><script>\n" +
                "  window.opener.postMessage({ \n" +
                "    error: 'LOGIN_FAILED', \n" +
                "    message: '" + errorMessage.replace("'", "\\'") + "'\n" +
                "  }, '" + allowedOrigins + "');\n" +
                "  window.close();\n" +
                "</script></body></html>";

        response.setContentType("text/html;charset=UTF-8");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        PrintWriter writer = response.getWriter();
        writer.write(script);
        writer.flush();
    }
}