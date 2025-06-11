package com.fondant.coupon.domain.entity;

import com.fondant.user.domain.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_coupon")
@Getter  // Getter 추가
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCouponEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_coupon_id")  // 컬럼명 명시
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")  // JoinColumn 추가
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")  // JoinColumn 추가
    private CouponEntity coupon;

    @Column(name = "is_used")  // 컬럼명 명시
    private boolean isUsed;

    @Column(name = "issued_at")  // 컬럼명 명시
    private LocalDateTime issuedAt;

    @Column(name = "used_at")  // 컬럼명 명시
    private LocalDateTime usedAt;

    // 사용 가능 여부 체크 메서드 추가
    public boolean isAvailable() {
        return !isUsed && coupon.isAvailable();
    }

    // 사용 시 검증 로직 강화
    public void markAsUsed() {
        if (isUsed) {
            throw new IllegalStateException("이미 사용된 쿠폰입니다.");
        }
        if (!coupon.isAvailable()) {
            throw new IllegalStateException("사용 가능한 기간이 아닙니다.");
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

