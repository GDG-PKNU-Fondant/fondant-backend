package com.fondant.user.presentation.dto.request;

import lombok.Builder;

@Builder
public record DeliveryAddressAddRequest(
        String deliveryAddress,
        Boolean isPrimary,
        String postCode,
        String alias,
        String receiverName,
        String receiverPhoneNumber
) {
}
