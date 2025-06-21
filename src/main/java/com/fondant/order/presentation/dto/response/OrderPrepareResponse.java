package com.fondant.order.presentation.dto.response;

import com.fondant.coupon.application.dto.CouponInfo;
import lombok.Builder;
import java.util.List;

@Builder
public record OrderPrepareResponse(
        List<PreparedOrderItemDto> orderItems,
        Double totalOrderPrice,
        List<DeliveryAddressDto> deliveryAddresses,
        List<CouponInfo> availableCoupons,
        int point
) {
    @Builder
    public record PreparedOrderItemDto(
            Long productId,
            String productName,
            String thumbnailUrl,
            Long optionId,
            String optionName,
            int quantity,
            Double price,
            Double optionPrice,
            Double discountRate,
            Double discountedPrice
    ) {}

    @Builder
    public record DeliveryAddressDto(
            Long id,
            String deliveryAddress,
            Boolean isPrimary,
            String postCode,
            String alias,
            String receiverName,
            String receiverPhoneNumber
    ) {}
} 