package com.fondant.user.domain.repository;

import com.fondant.user.domain.entity.SmsVerificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SmsVerificationRepository extends JpaRepository<SmsVerificationEntity, Long>, SmsVerificationRepositoryCustom {

    @Transactional
    void deleteByExpiresAtBefore(LocalDateTime now);

    Optional<SmsVerificationEntity> findByPhoneNumber(String phoneNumber);

    void deleteByPhoneNumber(String phoneNumber);
}
