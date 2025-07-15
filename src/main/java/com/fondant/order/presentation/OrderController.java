package com.fondant.order.presentation;

import com.fondant.global.annotation.CurrentUser;
import com.fondant.global.dto.ResponseDto;
import com.fondant.global.dto.SuccessMessage;
import com.fondant.order.application.OrderService;
import com.fondant.order.presentation.dto.request.OrderPrepareRequest;
import com.fondant.order.presentation.dto.request.OrderRequest;
import com.fondant.order.presentation.dto.response.OrderPrepareResponse;
import com.fondant.user.application.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ResponseDto<OrderResponse>> createOrder(
            @CurrentUser CustomUserDetails user,
            @RequestBody OrderRequest orderRequest) {
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS, orderService.createOrder(user, orderRequest)));
    }

    @GetMapping("/page-info")
    public ResponseEntity<ResponseDto<OrderPrepareResponse>> prepareOrder(
            @CurrentUser CustomUserDetails user,
            @RequestBody OrderPrepareRequest request
    ) {
        OrderPrepareResponse response = orderService.prepareOrder(user.getUserId(), request);
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS, response));
    }
}
