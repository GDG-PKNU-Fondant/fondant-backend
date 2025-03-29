package com.fondant.order.application;

import com.fondant.global.exception.ApiException;
import com.fondant.market.domain.entity.MarketEntity;
import com.fondant.market.domain.repository.MarketRepository;
import com.fondant.market.exception.MarketError;
import com.fondant.order.application.dto.OrderDetailCalculationResult;
import com.fondant.order.domain.entity.OrderDetailEntity;
import com.fondant.order.domain.entity.OrderEntity;
import com.fondant.order.domain.repository.OrderDetailRepository;
import com.fondant.order.domain.repository.OrderRepository;
import com.fondant.order.exception.OrderError;
import com.fondant.order.presentation.dto.request.OrderCreateRequest;
import com.fondant.order.presentation.dto.request.OrderItem;
import com.fondant.product.application.ProductService;
import com.fondant.product.domain.entity.OptionEntity;
import com.fondant.product.domain.entity.ProductEntity;
import com.fondant.product.domain.repository.OptionRepository;
import com.fondant.product.domain.repository.ProductRepository;
import com.fondant.product.exception.ProductError;
import com.fondant.user.domain.entity.UserEntity;
import com.fondant.user.domain.repository.UserRepository;
import com.fondant.user.exception.UserError;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private final UserRepository userRepository;
    private final MarketRepository marketRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final OptionRepository optionRepository;
    private final ProductService productService;

    public OrderService(UserRepository userRepository, MarketRepository marketRepository, OrderRepository orderRepository, OrderDetailRepository orderDetailRepository, ProductRepository productRepository, OptionRepository optionRepository, ProductService productService) {
        this.userRepository = userRepository;
        this.marketRepository = marketRepository;
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.productRepository = productRepository;
        this.optionRepository = optionRepository;
        this.productService = productService;
    }

    @Transactional
    public void createOrder(Long userId, OrderCreateRequest orderList) {
        LocalDateTime now = LocalDateTime.now();

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(UserError.USER_NOT_FOUND));

        OrderEntity order = OrderEntity.builder()
                .user(user)
                .orderDate(now)
                .delivery(null)
                .totalPrice(orderList.totalPrice())
                .deliveryAddress(user.findDeliveryAddressById(orderList.addressId()).getDeliveryAddress())
                .build();

        Map<MarketEntity, List<OrderItem>> itemsByMarket = divideOrderListByMarket(orderList);

        OrderDetailCalculationResult result = calculateOrderDetails(itemsByMarket, order);
        System.out.println(result);

        validateTotalPrice(result.totalItemPrice(), orderList.totalPrice());

        orderRepository.save(order);
        orderDetailRepository.saveAll(result.orderDetails());
    }

    private OrderDetailCalculationResult calculateOrderDetails(Map<MarketEntity, List<OrderItem>> itemsByMarket, OrderEntity order) {
        double itemTotalPrice = 0.0;
        List<OrderDetailEntity> orderDetails = new ArrayList<>();

        for (Map.Entry<MarketEntity, List<OrderItem>> entry : itemsByMarket.entrySet()) {
            MarketEntity market = entry.getKey();
            List<OrderItem> items = entry.getValue();

            boolean isFirstItem = true;
            for (OrderItem item : items) {
                ProductEntity product = productRepository.findById(item.productId())
                        .orElseThrow(() -> new ApiException(ProductError.PRODUCT_NOT_FOUND));

                OptionEntity option = optionRepository.findById(item.optionId())
                        .orElseThrow(() -> new ApiException(ProductError.OPTION_NOT_FOUND));

                double deliveryFee = isFirstItem? item.deliveryFee(): 0.0;
                isFirstItem = false;

                double itemPrice = (item.price() * (1.0 - product.getDiscountRate()) + item.optionPrice()) * item.quantity() + deliveryFee;

                itemTotalPrice += itemPrice;
                orderDetails.add(OrderDetailEntity.from(item, order, product, market));
            }
        }
        return OrderDetailCalculationResult.builder()
                .orderDetails(orderDetails)
                .totalItemPrice(itemTotalPrice)
                .build();
    }

    private void validateTotalPrice(double totalItemPrice, double orderTotalPrice) {
        if (totalItemPrice != orderTotalPrice) {
            throw new ApiException(OrderError.INVALID_ORDER);
        }
    }

    private HashMap<MarketEntity, List<OrderItem>> divideOrderListByMarket(OrderCreateRequest orderList) {
        HashMap<MarketEntity, List<OrderItem>> itemsByMarket = new HashMap<>();
        for (OrderItem item : orderList.items()) {
            MarketEntity market = marketRepository.findById(item.marketId())
                    .orElseThrow(() -> new ApiException(MarketError.MARKET_NOT_FOUND));

            itemsByMarket.computeIfAbsent(market, k -> new ArrayList<>()).add(item);
        }

        return itemsByMarket;
    }
}