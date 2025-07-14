package com.fondant.coupon.domain.repository;

import com.fondant.coupon.domain.entity.UserCouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCouponRepository extends JpaRepository<UserCouponEntity, Long>{
    boolean existsByUserIdAndCouponId(Long userId, Long couponId);
}
