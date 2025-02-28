package com.fondant.infra.oauth2.dto;

import com.fondant.user.domain.entity.SNSType;
import com.fondant.user.domain.entity.UserRole;

import java.util.Map;

public class GoogleResponse implements Oauth2Response {

    private final Map<String, Object> attributes;

    public GoogleResponse(final Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProvider() {
        return SNSType.GOOGLE.name();
    }

    @Override
    public String getEmail() {
        return attributes.get("email").toString();
    }

    @Override
    public String getName() {
        return attributes.get("name").toString();
    }

    @Override
    public String getRole() {
        return UserRole.USER.toString();
    }

    @Override
    public String getPhoneNumber() {
        return "";
    }
}
