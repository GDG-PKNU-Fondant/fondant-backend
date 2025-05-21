package com.fondant.cart.application.dto;

import lombok.Builder;

@Builder
public record CartOptionInfo (
        Long optionId,
        String optionName,
        Double additionalPrice,
        int quantity
) {}

