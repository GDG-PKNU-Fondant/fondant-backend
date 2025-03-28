package com.fondant.order.application.dto;

import com.fondant.order.domain.entity.OrderDetailEntity;
import lombok.Builder;

import java.util.List;

@Builder
public record OrderDetailCalculationResult(
        double totalItemPrice,
        List<OrderDetailEntity> orderDetails
) {
}
