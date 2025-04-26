package com.fondant.product.application.dto;

import java.util.List;
import java.util.Optional;

public record FilterInfo(
        Optional<Double> startPrice,
        Optional<Double> endPrice,
        List<Long> categoryIds,
        List<String> packingTypes,
        List<String> benefitTypes

) {
}