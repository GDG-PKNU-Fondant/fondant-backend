package com.fondant.coupon.domain.repository;

import com.fondant.coupon.domain.entity.UserCouponEntity;

import java.util.List;
import java.util.Set;

public interface UserCouponRepositoryCustom {
    List<UserCouponEntity> findValidUserCoupons(Long userId, Set<Long> couponIds);
}