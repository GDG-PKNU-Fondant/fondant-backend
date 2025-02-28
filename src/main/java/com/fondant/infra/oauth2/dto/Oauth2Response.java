package com.fondant.infra.oauth2.dto;

public interface Oauth2Response {

    String getProvider();

    String getEmail();

    String getName();

    String getRole();

    String getPhoneNumber();

}
