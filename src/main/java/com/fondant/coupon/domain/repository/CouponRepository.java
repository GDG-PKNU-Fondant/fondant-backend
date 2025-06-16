package com.fondant.coupon.domain.repository;

import com.fondant.coupon.domain.entity.CouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponRepository extends JpaRepository<CouponEntity, Long>, CouponRepositoryCustom {
}
