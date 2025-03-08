package com.fondant.user.application;

import com.fondant.global.exception.ApiException;
import com.fondant.user.domain.entity.SmsVerificationEntity;
import com.fondant.user.domain.repository.SmsVerificationRepository;
import com.fondant.user.exception.UserError;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class SmsVerificationService {

    @Value("${spring.cool-sms.api-key}")
    private String apiKey;

    @Value("${spring.cool-sms.api-secret}")
    private String apiSecret;

    @Value("${spring.cool-sms.caller-number}")
    private String callerNumber;

    private final SmsVerificationRepository smsVerificationRepository;

    LocalDateTime now = LocalDateTime.now();

    public SmsVerificationService(SmsVerificationRepository smsVerificationRepository) {
        this.smsVerificationRepository = smsVerificationRepository;
    }

    public void sendMessage(String phoneNumber) throws Exception {

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            throw new ApiException(UserError.ADDRESS_NOT_FOUND);
        }

        String purePhoneNumber = removeHyphens(phoneNumber);

        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        int randomNumber = 100_000 + secureRandom.nextInt(900_000);

        SmsVerificationEntity entity = SmsVerificationEntity.builder()
                .phoneNumber(purePhoneNumber)
                .createdAt(now)
                .expiresAt(now.plusMinutes(1))
                .verificationCode(String.valueOf(randomNumber))
                .build();

        smsVerificationRepository.save(entity);

        sendVerificationCode(purePhoneNumber, String.valueOf(randomNumber));
    }

    private void sendVerificationCode(String phoneNumber, String verificationCode) throws Exception {
        DefaultMessageService messageService =  NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");
        Message message = new Message();
        message.setFrom(callerNumber);
        message.setTo(phoneNumber);
        message.setText("[퐁당] 인증번호 [" + verificationCode + "]를 입력해주세요.");

        try {
            messageService.send(message);
        } catch (NurigoMessageNotReceivedException exception) {
            System.out.println(exception.getFailedMessageList());
            throw new ApiException(UserError.SMS_SEND_FAILED);
        }
    }

    public void verifyCode(String phoneNumber, String code){

        smsVerificationRepository.deleteByExpiresAtBefore(now);

        SmsVerificationEntity entity = smsVerificationRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ApiException(UserError.VERIFICATION_NOT_FOUND));

        if (entity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApiException(UserError.VERIFICATION_IS_EXPIRED);
        }

        if (!entity.getVerificationCode().equals(code)) {
            throw new ApiException(UserError.VERIFICATION_NOT_MATCH);
        }

        smsVerificationRepository.delete(entity);
    }

    public String removeHyphens(String phoneNumber) {
        return phoneNumber.replace("-", "");
    }

}
