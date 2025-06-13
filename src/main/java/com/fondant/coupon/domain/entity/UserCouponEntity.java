package com.fondant.coupon.domain.entity;

import com.fondant.coupon.exception.CouponError;
import com.fondant.global.exception.ApiException;
import com.fondant.user.domain.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_coupon")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCouponEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_coupon_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private CouponEntity coupon;

    @Column(name = "is_used")
    private boolean isUsed;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    public boolean isAvailable() {
        return !isUsed && coupon.isAvailable();
    }

    public void markAsUsed() {
        if (isUsed) {
            throw new ApiException(CouponError.ALREADY_USED_COUPON);
        }
        if (!coupon.isAvailable()) {
            throw new ApiException(CouponError.COUPON_NOT_AVAILABLE);
        }
        this.isUsed = true;
        this.usedAt = LocalDateTime.now();
    }

    @Builder
    public UserCouponEntity(UserEntity user, CouponEntity coupon) {
        this.user = user;
        this.coupon = coupon;
        this.isUsed = false;
        this.issuedAt = LocalDateTime.now();
    }
}

