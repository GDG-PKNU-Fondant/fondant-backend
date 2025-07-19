package com.fondant.payment.presentation;

import com.fondant.global.annotation.CurrentUser;
import com.fondant.global.dto.ResponseDto;
import com.fondant.global.dto.SuccessMessage;
import com.fondant.payment.application.PaymentService;
import com.fondant.payment.presentation.dto.PaymentRequest;
import com.fondant.payment.presentation.dto.PaymentResponse;
import com.fondant.user.application.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/complete")
    public ResponseEntity<ResponseDto<PaymentResponse>> complete(
            @CurrentUser CustomUserDetails user,
            @RequestBody PaymentRequest request
    ) {
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS,
                paymentService.completePayment(user, request)));
    }

    @DeleteMapping("/{paymentId}")
    public ResponseEntity<ResponseDto<Void>> cancelPayment(@PathVariable String paymentId) {
        paymentService.cancelPayment(paymentId);
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS));
    }
}
