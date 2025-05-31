package com.fondant.payment.presentation;

import com.fondant.global.dto.ResponseDto;
import com.fondant.payment.presentation.dto.PaymentResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/verify-and-order")
public class PaymentController {
    @GetMapping
    public ResponseEntity<ResponseDto<PaymentResponse>> paymentOrder(PaymentRequest) {
        paymentService.
    }
}
