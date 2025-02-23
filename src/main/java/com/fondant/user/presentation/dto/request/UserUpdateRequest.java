package com.fondant.user.presentation.dto.request;

import com.fondant.user.domain.entity.Gender;
import lombok.Builder;

import java.sql.Date;

@Builder
public record UserUpdateRequest(
        String name,
        String phoneNumber,
        boolean verifiedPhone,
        String email,
        Date birth,
        String nickname,
        String profileUrl,
        Gender gender
) {
}
