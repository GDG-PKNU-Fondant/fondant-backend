package com.fondant.coupon.domain.repository;

import com.fondant.coupon.domain.entity.UserCouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface UserCouponRepository extends JpaRepository<UserCouponEntity, Long>, UserCouponRepositoryCustom {
    boolean existsByUserIdAndCouponId(Long userId, Long couponId);
    List<UserCouponEntity> findValidUserCoupons(Long userId, Set<Long> couponIds);
}
