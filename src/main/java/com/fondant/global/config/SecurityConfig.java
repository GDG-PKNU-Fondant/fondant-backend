package com.fondant.global.config;

import com.fondant.infra.jwt.filter.CustomLogoutFilter;
import com.fondant.infra.jwt.filter.JWTFilter;
import com.fondant.infra.jwt.application.JWTUtil;
import com.fondant.infra.jwt.filter.LoginFilter;

import com.fondant.infra.jwt.domain.repository.RefreshRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import java.util.Collections;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository;


    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration, JWTUtil jwtUtil, RefreshRepository refreshRepository) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable);

        http
                .cors((cors) -> cors
                        .configurationSource(request -> {
                            CorsConfiguration configration = new CorsConfiguration();
                            configration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
                            configration.setAllowedMethods(Collections.singletonList("*"));
                            configration.setAllowCredentials(true);
                            configration.setAllowedHeaders(Collections.singletonList("*"));
                            configration.setMaxAge(3600L);
                            configration.setExposedHeaders(Collections.singletonList("Authorization"));
                            return configration;
                        }));

        http
                .formLogin(AbstractHttpConfigurer::disable);

        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/api/user/join", "/api/user/login", "/api/user/reissue").permitAll()
                        .requestMatchers("/admin").hasRole("ADMIN")
                        .anyRequest().authenticated());

        http
                .addFilterBefore(new JWTFilter(jwtUtil), LoginFilter.class);

        http
                .addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil, refreshRepository), UsernamePasswordAuthenticationFilter.class);

        http
                .addFilterAt(new CustomLogoutFilter(jwtUtil, refreshRepository), LogoutFilter.class);

        http
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
