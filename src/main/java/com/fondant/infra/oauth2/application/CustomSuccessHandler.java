package com.fondant.infra.oauth2.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fondant.infra.jwt.application.JWTUtil;
import com.fondant.infra.oauth2.dto.CustomOAuth2User;
import com.fondant.user.domain.entity.SNSType;
import com.fondant.user.domain.entity.UserEntity;
import com.fondant.user.domain.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import static org.springframework.scheduling.config.TaskExecutionOutcome.Status.SUCCESS;

@Component
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private JWTUtil jwtUtil;
    private UserRepository userRepository;

    public CustomSuccessHandler(JWTUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        Optional<UserEntity> userEntity = userRepository.findByEmail(customUserDetails.getEmail());

        UserEntity user = userEntity.orElseThrow(() -> new UsernameNotFoundException("유저가 존재하지 않습니다."));

        Long userId = userEntity.get().getId();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        String accessToken = jwtUtil.generateToken("access", userId, role, 60 * 60 * 24 * 1000L);
        String refreshToken = jwtUtil.generateToken("refresh", userId, role, 60 * 60 * 24 * 1000L);

        response.addCookie(createCookie("refresh", refreshToken));

        String script = "<!DOCTYPE html><html><body><script>\n" +
                "  const accessToken = '" + accessToken + "';\n" +
                "  window.opener.postMessage({ accessToken }, 'http://localhost:5173');\n" +
                "  window.close();\n" +
                "</script></body></html>";

        response.setContentType("text/html;charset=UTF-8");
        response.setStatus(HttpStatus.OK.value());
        PrintWriter writer = response.getWriter();
        writer.write(script);
        writer.flush();
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
