package com.fondant.product.application.dto;

import com.fondant.product.domain.entity.PackagingType;

import java.util.List;
import java.util.Optional;

public record FilterInfo(
        Optional<Double> startPrice,
        Optional<Double> endPrice,
        List<Long> categoryIds,
        List<PackagingType> packagingTypes,
        List<String> benefitTypes

) {
}