package com.fondant.coupon.domain.repository;

import com.fondant.coupon.domain.entity.CouponEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface CouponRepositoryCustom {
    Slice<CouponEntity> findIssuableCoupons(Long userId, Pageable pageable);
    Slice<CouponEntity> findIssuedCoupons(Long userId, Pageable pageable);
}
