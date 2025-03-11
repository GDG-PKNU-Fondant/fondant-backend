package com.fondant.global.factory;

import com.fondant.infra.jwt.dto.JWTUserDTO;
import com.fondant.test.repository.UserTestRepository;
import com.fondant.global.annotation.WithMockCustomUser;
import com.fondant.user.application.dto.CustomUserDetails;
import com.fondant.user.domain.entity.Gender;
import com.fondant.user.domain.entity.SNSType;
import com.fondant.user.domain.entity.UserEntity;
import com.fondant.user.domain.entity.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDate;

@Component
public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

    @Autowired
    private UserTestRepository userRepository;

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        UserEntity user = UserEntity.builder()
                .name("test")
                .email("test@example.com")
                .birth(Date.valueOf(LocalDate.of(2025, 1, 1)))
                .gender(Gender.MALE)
                .profileUrl("https://example.com/profile.png")
                .phoneNumber("010-1234-5678")
                .verifiedPhone(true)
                .nickname("tester123")
                .password("password")
                .snsType(SNSType.LOCAL)
                .role(UserRole.USER)
                .createAt(LocalDate.now())
                .build();

        user = userRepository.save(user);

        CustomUserDetails userDetails = new CustomUserDetails(
                JWTUserDTO.builder()
                        .userId(user.getId())
                        .userEmail(user.getEmail())
                        .role(user.getRole().toString())
                        .build()
        );

        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        context.setAuthentication(auth);

        SecurityContextHolder.setContext(context);

        return context;
    }
}