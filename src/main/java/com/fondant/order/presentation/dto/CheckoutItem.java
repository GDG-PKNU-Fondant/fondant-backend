package com.fondant.order.presentation.dto;

public record CheckoutItem (
    Long optionId,
    Long productId,
    int quantity,
    Long deliveryFee
){
}
