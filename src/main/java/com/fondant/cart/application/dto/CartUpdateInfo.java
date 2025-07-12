package com.fondant.cart.application.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record CartUpdateInfo(
        int quantity,
        List<OptionUpdateInfo> options
) {
    @Builder
    public record OptionUpdateInfo(
            Long optionId,
            int quantity
    ) {}
}