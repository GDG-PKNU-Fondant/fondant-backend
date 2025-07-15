package com.fondant.infra.portone.util;

import com.fondant.infra.portone.dto.CancellationResponse;
import com.fondant.order.presentation.dto.request.OrderDetails;
import com.fondant.payment.application.dto.PaymentDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PortOneApiClient {
    private final WebClient portOneWebClient;

    @Value("${portone.api-secret}")
    private String apiToken;

    public PaymentDetails getPaymentDetails(String paymentId) {
        return portOneWebClient
                .get()
                .uri("/{paymentId}", paymentId)
                .header(HttpHeaders.AUTHORIZATION, "PortOne " + apiToken)
                .retrieve()
                .bodyToMono(PaymentDetails.class)
                .block();
    }

    public CancellationResponse cancelPayment(String paymentId) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("reason", "고객 요청으로 인한 취소");

        return portOneWebClient
                .post()
                .uri("/{paymentId}/cancel", paymentId)
                .header("Authorization", "PortOne " + apiToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(CancellationResponse.class)
                .block();
    }
}
