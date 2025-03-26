package com.fondant.user.application;

import com.fondant.global.exception.ApiException;
import com.fondant.infra.sms.provider.CoolSmsProvider;
import com.fondant.user.domain.entity.SmsVerificationEntity;
import com.fondant.user.domain.repository.SmsVerificationRepository;
import com.fondant.user.exception.UserError;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@Transactional
public class SmsVerificationService {

    private final CoolSmsProvider coolSmsProvider;

    private final SmsVerificationRepository smsVerificationRepository;

    public SmsVerificationService(SmsVerificationRepository smsVerificationRepository, CoolSmsProvider coolSmsProvider) {
        this.smsVerificationRepository = smsVerificationRepository;
        this.coolSmsProvider = coolSmsProvider;
    }

    public void sendMessage(String phoneNumber) throws Exception {

        LocalDateTime now = LocalDateTime.now();

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            throw new ApiException(UserError.ADDRESS_NOT_FOUND);
        }

        String purePhoneNumber = removeHyphens(phoneNumber);

        smsVerificationRepository.deleteByPhoneNumber(purePhoneNumber);

        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        int randomNumber = 100_000 + secureRandom.nextInt(900_000);

        SmsVerificationEntity entity = SmsVerificationEntity.builder()
                .phoneNumber(purePhoneNumber)
                .createdAt(now)
                .expiresAt(now.plusMinutes(3))
                .verificationCode(String.valueOf(randomNumber))
                .build();

        smsVerificationRepository.save(entity);

        coolSmsProvider.sendVerificationCode(purePhoneNumber, String.valueOf(randomNumber));
    }

    public void verifyCode(String phoneNumber, String code){

        String purePhoneNumber = removeHyphens(phoneNumber);

        SmsVerificationEntity verificationEntity = smsVerificationRepository.findByPhoneNumber(purePhoneNumber)
                .orElseThrow(() -> new ApiException(UserError.VERIFICATION_NOT_FOUND));

        if (isExpired(verificationEntity)) {
            throw new ApiException(UserError.VERIFICATION_IS_EXPIRED);
        }

        if (!isEqual(verificationEntity, code)) {
            throw new ApiException(UserError.VERIFICATION_NOT_MATCH);
        }

        smsVerificationRepository.delete(verificationEntity);
    }

    public String removeHyphens(String phoneNumber) {
        return phoneNumber.replace("-", "");
    }

    public boolean isExpired(SmsVerificationEntity verificationEntity) {
        LocalDateTime expiresAt = verificationEntity.getExpiresAt();
        return expiresAt.isBefore(LocalDateTime.now());
    }

    public boolean isEqual(SmsVerificationEntity verificationEntity, String code) {
        return verificationEntity.getVerificationCode().equals(code);
    }
}
