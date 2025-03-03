package com.fondant.user.presentation.dto.response;

import lombok.Builder;

@Builder
public record DeliveryAddressResponse(
        Long id,
        String deliveryAddress,
        Boolean isPrimary,
        String postCode,
        String alias,
        String receiverName,
        String receiverPhoneNumber
) {
}
