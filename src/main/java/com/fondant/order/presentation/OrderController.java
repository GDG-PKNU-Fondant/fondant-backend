package com.fondant.order.presentation;

import com.fondant.global.annotation.CurrentUser;
import com.fondant.global.dto.ResponseDto;
import com.fondant.global.dto.SuccessMessage;
import com.fondant.order.application.OrderService;
import com.fondant.order.presentation.dto.request.OrderCreateRequest;
import com.fondant.user.application.dto.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("")
    public ResponseEntity<ResponseDto<Void>> createOrder(@CurrentUser CustomUserDetails user, @RequestBody OrderCreateRequest orderList) {
        orderService.createOrder(user.getUserId(), orderList);
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS));
    }
}
