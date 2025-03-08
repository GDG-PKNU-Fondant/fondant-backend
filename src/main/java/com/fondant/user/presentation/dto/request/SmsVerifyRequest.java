package com.fondant.user.presentation.dto.request;

import lombok.Builder;

@Builder
public record SmsVerifyRequest(
        String phoneNumber,
        String code
) {
}
