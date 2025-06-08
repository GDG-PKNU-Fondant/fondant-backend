package com.fondant.cart.application.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record CartProductInfo (
        Long productId,
        String productName,
        String thumbnail,
        List<CartOptionInfo> options,
        int quantity,
        LocalDate arrivalDate
) {}
