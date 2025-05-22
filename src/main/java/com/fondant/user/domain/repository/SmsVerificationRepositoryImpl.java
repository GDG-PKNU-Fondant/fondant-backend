package com.fondant.infra.sms.domain.repository;

import com.fondant.global.exception.ApiException;
import com.fondant.user.domain.entity.QSmsVerificationEntity;
import com.fondant.user.exception.UserError;
import com.querydsl.jpa.impl.JPAQueryFactory;

import java.time.LocalDateTime;

public class SmsVerificationRepositoryImpl implements SmsVerificationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public SmsVerificationRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public void deleteByExpiresAtBefore(LocalDateTime now) {
        QSmsVerificationEntity entity = QSmsVerificationEntity.smsVerificationEntity;

        try {
            queryFactory.delete(entity)
                    .where(entity.expiresAt.before(now))
                    .execute();
        } catch (Exception e) {
            throw new ApiException(UserError.VERIFICATION_NOT_DELETED);
        }
    }
}
