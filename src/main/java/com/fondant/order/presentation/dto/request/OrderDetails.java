package com.fondant.order.presentation.dto.request;

import com.fondant.order.presentation.dto.CheckoutItem;
import com.fondant.order.presentation.dto.CouponApplyDto;

import java.util.List;

public record OrderDetails(
        List<CheckoutItem> checkoutItems,
        List<CouponApplyDto> couponAllies,
        Integer discountPoints,
        PaymentMethod payment,
        Long deliveryAddressId,
        Integer expectedAmount,
        Long cartId
) {
}