package com.fondant.infra.sms.domain.repository;

import java.time.LocalDateTime;

public interface SmsVerificationRepositoryCustom {

    void deleteByExpiresAtBefore(LocalDateTime now);
}
