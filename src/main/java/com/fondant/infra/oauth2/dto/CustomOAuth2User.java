package com.fondant.infra.oauth2.dto;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private final OAuth2UserDTO oAuth2UserDTO;

    public CustomOAuth2User(final OAuth2UserDTO oAuth2UserDTO) {
        this.oAuth2UserDTO = oAuth2UserDTO;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> authorities = new ArrayList<>();

        authorities.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return oAuth2UserDTO.role();
            }
        });

        return authorities;
    }

    @Override
    public String getName() {
        return oAuth2UserDTO.name();
    }

    public String getProvider() {
        return oAuth2UserDTO.provider();
    }

    public String getEmail() {
        return oAuth2UserDTO.email();
    }
}
