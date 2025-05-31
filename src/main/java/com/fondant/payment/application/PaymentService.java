package com.fondant.payment.application;

import com.fondant.payment.application.dto.PaymentInfo;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    public PaymentInfo paymentOrder() {
        return new PaymentInfo();
    }
}
