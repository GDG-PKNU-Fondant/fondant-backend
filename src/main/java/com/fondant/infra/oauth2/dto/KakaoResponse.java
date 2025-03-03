package com.fondant.infra.oauth2.dto;

import com.fondant.user.domain.entity.SNSType;
import com.fondant.user.domain.entity.UserRole;

import java.util.Map;

public class KakaoResponse implements Oauth2Response {

    private final Map<String, Object> attributes;

    public KakaoResponse(final Map<String, Object> attributes) {
        this.attributes = (Map<String, Object>) attributes.get("response");
    }

    @Override
    public String getProvider() {
        return SNSType.KAKAO.name();
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
