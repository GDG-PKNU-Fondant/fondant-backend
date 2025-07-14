package com.fondant.coupon.application;

import com.fondant.coupon.application.dto.CouponInfo;
import com.fondant.coupon.domain.entity.UserCouponEntity;
import com.fondant.coupon.domain.repository.UserCouponRepository;
import com.fondant.coupon.domain.entity.CouponEntity;
import com.fondant.coupon.domain.repository.CouponRepository;
import com.fondant.coupon.exception.CouponError;
import com.fondant.coupon.presentation.dto.response.CouponListResponse;
import com.fondant.global.dto.SliceInfo;
import com.fondant.global.exception.ApiException;
import com.fondant.user.domain.entity.UserEntity;
import com.fondant.user.domain.repository.UserRepository;
import com.fondant.user.exception.UserError;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final UserRepository userRepository;

    public CouponListResponse getIssuableCoupons(Long userId, Pageable pageable) {
        Slice<CouponEntity> couponSlice = couponRepository.findIssuableCoupons(userId, pageable);

        List<CouponInfo> coupons = couponSlice
                .map(CouponInfo::from)
                .getContent();

        SliceInfo sliceInfo = SliceInfo.of(couponSlice.hasNext());

        return CouponListResponse.of(coupons, sliceInfo);
    }

    public CouponListResponse getIssuedCoupons(Long userId, Pageable pageable) {
        Slice<CouponEntity> couponSlice = couponRepository.findIssuedCoupons(userId, pageable);

        List<CouponInfo> coupons = couponSlice
                .map(CouponInfo::from)
                .getContent();

        SliceInfo sliceInfo = SliceInfo.of(couponSlice.hasNext());

        return CouponListResponse.of(coupons, sliceInfo);
    }

    @Transactional
    public void issueCoupon(Long userId, Long couponId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(UserError.USER_NOT_FOUND));

        CouponEntity coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ApiException(CouponError.COUPON_NOT_FOUND));

        if (!coupon.isAvailable()) {
            throw new ApiException(CouponError.COUPON_NOT_AVAILABLE);
        }

        if (userCouponRepository.existsByUserIdAndCouponId(userId, couponId)) {
            throw new ApiException(CouponError.ALREADY_USED_COUPON);
        }

        UserCouponEntity userCoupon = UserCouponEntity.builder()
                .user(user)
                .coupon(coupon)
                .build();

        userCouponRepository.save(userCoupon);
    }
}