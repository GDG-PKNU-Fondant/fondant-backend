package com.fondant.test.repository;

import com.fondant.user.domain.entity.SmsVerificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SmsVerificationTestRepository extends JpaRepository<SmsVerificationEntity, Long>{
}
