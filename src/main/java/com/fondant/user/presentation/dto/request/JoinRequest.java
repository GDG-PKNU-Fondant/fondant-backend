package com.fondant.user.presentation.dto.request;

import com.fondant.user.domain.entity.Gender;
import lombok.Builder;

import java.sql.Date;

@Builder
public record JoinRequest (
    String name,
    String phoneNumber,
    String email,
    String password,
    Date birth,
    Gender gender){
}