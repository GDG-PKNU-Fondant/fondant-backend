package com.fondant.infra.jwt.dto;

import lombok.Builder;

@Builder
public record JWTUserDTO(
        String userEmail,
        String password,
        String userId,
        String role
) {
}
