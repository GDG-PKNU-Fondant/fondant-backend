package com.fondant.user.domain.repository;

import java.time.LocalDateTime;

public interface SmsVerificationRepositoryCustom {

    void deleteByExpiresAtBefore(LocalDateTime now);
}
