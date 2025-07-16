package com.fondant.coupon.util;

import com.fondant.coupon.domain.entity.CouponEntity;
import com.fondant.coupon.domain.entity.DiscountType;
import org.springframework.stereotype.Component;

@Component
public class CouponUtil {

    public int calcDiscount(CouponEntity coupon, int subTotal) {
        if (coupon.getDiscountType() == DiscountType.AMOUNT) {
            return Math.min(coupon.getDiscountAmount().intValue(), subTotal);
        } else if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            return (int) Math.floor(subTotal * (coupon.getDiscountAmount() / 100.0));
        }
        return 0;
    }
}
