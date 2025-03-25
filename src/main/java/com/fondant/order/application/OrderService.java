package com.fondant.order.application;

import com.fondant.global.exception.ApiException;
import com.fondant.order.domain.entity.OrderDetailEntity;
import com.fondant.order.domain.entity.OrderEntity;
import com.fondant.order.domain.entity.QOrderDetailEntity;
import com.fondant.order.presentation.dto.request.OrderCreateRequest;
import com.fondant.order.presentation.dto.request.OrderItem;
import com.fondant.user.domain.entity.UserEntity;
import com.fondant.user.domain.repository.UserRepository;
import com.fondant.user.exception.UserError;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final UserRepository userRepository;

    public OrderService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Void createOrder(Long userId, OrderCreateRequest orderList) {
        LocalDateTime now = LocalDateTime.now();

        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new ApiException(UserError.USER_NOT_FOUND));

        OrderEntity order = OrderEntity.builder()
                .user(user)
                .orderDate(now)
                .deliveryAddress(user.findDeliveryAddressById(orderList.addressId()))
                .build();

        List<OrderDetailEntity> orderDetailList = orderList.items().stream()
                .map(items -> OrderDetailEntity.builder()
                        .product(items.productName())
                        .orderEntity(order)
                        .option(items.optionId())
                        .
                        .build())
                .collect(Collectors.toList());

        OrderDetailEntity orderDetail = OrderDetailEntity.builder().build();

    }
}
