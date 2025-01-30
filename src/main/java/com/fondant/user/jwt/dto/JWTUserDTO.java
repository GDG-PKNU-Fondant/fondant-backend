package com.fondant.user.jwt.dto;

import lombok.Builder;

@Builder
public record JWTUserDTO(
        String userEmail,
        String password,
        String userId,
        String role
) {
}
