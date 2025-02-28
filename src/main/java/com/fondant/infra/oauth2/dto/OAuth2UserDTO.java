package com.fondant.infra.oauth2.dto;

import lombok.Builder;

@Builder
public record OAuth2UserDTO(
        String name,
        String email,
        String provider,
        String role,
        String phoneNumber
) {
}