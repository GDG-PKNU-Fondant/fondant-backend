package com.fondant.order.presentation.dto;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public record CouponValidationResult(
        int totalDiscount,
        Set<Long> usedCouponIds,
        Map<Long, Long> couponToItemMap) {

    public CouponValidationResult() {
        this(0, Collections.emptySet(), Map.of());
    }
}
