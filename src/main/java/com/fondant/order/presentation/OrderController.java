package com.fondant.order.presentation;

import com.fondant.global.annotation.CurrentUser;
import com.fondant.global.dto.ResponseDto;
import com.fondant.global.dto.SuccessMessage;
import com.fondant.order.application.OrderService;
import com.fondant.order.presentation.dto.request.OrderCreateRequest;
import com.fondant.order.presentation.dto.request.OrderPrepareRequest;
import com.fondant.order.presentation.dto.response.OrderPrepareResponse;
import com.fondant.user.application.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("")
    public ResponseEntity<ResponseDto<Void>> createOrder(@CurrentUser CustomUserDetails user, @RequestBody OrderCreateRequest orderList) {
        orderService.createOrder(user.getUserId(), orderList);
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS));
    }

    @PostMapping("/prepare")
    public ResponseEntity<ResponseDto<OrderPrepareResponse>> prepareOrder(
            @CurrentUser CustomUserDetails user,
            @RequestBody OrderPrepareRequest request
    ) {
        OrderPrepareResponse response = orderService.prepareOrder(user.getUserId(), request);
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS, response));
    }
}
