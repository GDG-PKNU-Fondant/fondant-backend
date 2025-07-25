package com.fondant.coupon.domain.repository;

import com.fondant.coupon.domain.entity.QCouponEntity;
import com.fondant.coupon.domain.entity.QUserCouponEntity;
import com.fondant.coupon.domain.entity.UserCouponEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class UserCouponRepositoryImpl implements UserCouponRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<UserCouponEntity> findValidUserCoupons(Long userId, Set<Long> couponIds) {
        QUserCouponEntity userCoupon = QUserCouponEntity.userCouponEntity;
        QCouponEntity coupon = QCouponEntity.couponEntity;
        LocalDateTime now = LocalDateTime.now();

        return queryFactory
                .selectFrom(userCoupon)
                .join(userCoupon.coupon, coupon)
                .where(
                        userCoupon.user.id.eq(userId),
                        userCoupon.coupon.id.in(couponIds),
                        userCoupon.isUsed.isFalse(),
                        coupon.startDate.loe(now),
                        coupon.endDate.goe(now)
                )
                .fetch();
    }
}
