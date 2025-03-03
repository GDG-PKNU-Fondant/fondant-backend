package com.fondant.infra.jwt.dto;

import lombok.Builder;

@Builder
public record JWTUserDTO(
        String userEmail,
        String password,
        Long userId,
        String role
) {
}
