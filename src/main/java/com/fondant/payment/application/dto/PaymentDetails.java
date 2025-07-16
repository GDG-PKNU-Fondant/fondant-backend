package com.fondant.payment.application.dto;

import com.fondant.order.presentation.dto.request.OrderDetails;

public record PaymentDetails(
    String paymentId,
    Long orderId,
    String orderName,
    Integer totalAmount,
    String method,
    PayCustomer customer,
    OrderDetails orderDetails
) {
    private record PayCustomer(
            String userId,
            String userEmail
    ){
    }
}