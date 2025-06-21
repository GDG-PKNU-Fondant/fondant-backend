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
import com.fondant.order.presentation.dto.request.OrderPrepareRequest;
import com.fondant.order.presentation.dto.response.OrderPrepareResponse;
import com.fondant.product.application.ProductService;
import com.fondant.product.domain.entity.OptionEntity;
import com.fondant.product.domain.entity.ProductEntity;
import com.fondant.product.domain.repository.OptionRepository;
import com.fondant.product.domain.repository.ProductRepository;
import com.fondant.product.exception.ProductError;
import com.fondant.user.application.UserService;
import com.fondant.user.domain.entity.UserEntity;
import com.fondant.user.domain.repository.UserRepository;
import com.fondant.user.exception.UserError;
import com.fondant.coupon.application.CouponService;
import com.fondant.coupon.application.dto.CouponInfo;
import com.fondant.coupon.presentation.dto.response.CouponListResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private final MarketRepository marketRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final OptionRepository optionRepository;
    private final ProductService productService;
    private final CouponService couponService;
    private final UserService userService;

    public OrderService(MarketRepository marketRepository, OrderRepository orderRepository,
                        OrderDetailRepository orderDetailRepository, ProductRepository productRepository,
                        OptionRepository optionRepository, ProductService productService,
                        CouponService couponService, UserService userService) {
        this.marketRepository = marketRepository;
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.productRepository = productRepository;
        this.optionRepository = optionRepository;
        this.productService = productService;
        this.couponService = couponService;
        this.userService = userService;
    }

    @Transactional
    public OrderPrepareResponse prepareOrder(Long userId, OrderPrepareRequest request) {
        UserEntity user = userService.getUserEntityById(userId);

        List<OrderPrepareResponse.PreparedOrderItemDto> preparedItems = new ArrayList<>();
        double totalOrderPrice = 0.0;

        for (OrderPrepareRequest.OrderItem item : request.getItems()) {

            OptionEntity option = productService.findOptionByIdAndProductId(item.optionId(), item.productId());
            ProductEntity product = productService.findProductById(item.productId());

            double discountedPrice = productService.getDiscountedPrice(product.getPrice() + option.getPrice(), product.getDiscountRate());

            totalOrderPrice += discountedPrice * item.quantity();

            preparedItems.add(
                    toPreparedOrderItemDto(product, option, item, discountedPrice)
            );
        }

        List<OrderPrepareResponse.DeliveryAddressDto> deliveryAddresses = toDeliveryAddressDtos(user);

        CouponListResponse couponResponse = couponService.getIssuedCoupons(userId, Pageable.unpaged());
        List<CouponInfo> availableCoupons = couponResponse.coupons();

        int userPoint = user.getPoint();

        return OrderPrepareResponse.builder()
                .orderItems(preparedItems)
                .totalOrderPrice(totalOrderPrice)
                .deliveryAddresses(deliveryAddresses)
                .availableCoupons(availableCoupons)
                .point(userPoint)
                .build();
    }

    @Transactional
    public void createOrder(Long userId, OrderCreateRequest orderList) {
        LocalDateTime now = LocalDateTime.now();

        UserEntity user = userService.getUserEntityById(userId);

        OrderEntity order = OrderEntity.builder()
                .user(user)
                .orderDate(now)
                .delivery(null)
                .totalPrice(orderList.totalPrice())
                .deliveryAddress(userService.findDeliveryAddressById(userId, orderList.addressId()).getDeliveryAddress())
                .build();

        Map<MarketEntity, List<OrderItem>> itemsByMarket = divideOrderListByMarket(orderList);

        OrderDetailCalculationResult result = calculateOrderDetails(itemsByMarket, order);

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
                OptionEntity option = productService.findOptionByIdAndProductId(item.optionId(), item.productId());
                ProductEntity product = productService.findProductById(item.productId());

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

    public static double calculateTotalPrice(double price, double optionPrice, int quantity, double discountRate) {
        return Math.round(price * (1.0 - discountRate) + optionPrice) * quantity;
    }

    private OrderPrepareResponse.PreparedOrderItemDto toPreparedOrderItemDto(
            ProductEntity product, OptionEntity option, OrderPrepareRequest.OrderItem item, double discountedPrice) {

        return OrderPrepareResponse.PreparedOrderItemDto.builder()
                .productId(product.getId())
                .productName(product.getName())
                .thumbnailUrl(product.getThumbnail())
                .optionId(option.getId())
                .optionName(option.getName())
                .quantity(item.quantity())
                .price(product.getPrice())
                .optionPrice(option.getPrice())
                .discountRate(product.getDiscountRate())
                .discountedPrice(discountedPrice)
                .build();
    }

    private List<OrderPrepareResponse.DeliveryAddressDto> toDeliveryAddressDtos(UserEntity user) {
        return user.getDeliveryAddresses().stream()
                .map(addr -> OrderPrepareResponse.DeliveryAddressDto.builder()
                        .id(addr.getId())
                        .deliveryAddress(addr.getDeliveryAddress())
                        .isPrimary(addr.getIsPrimary())
                        .postCode(addr.getPostCode())
                        .alias(addr.getAlias())
                        .receiverName(addr.getReceiverName())
                        .receiverPhoneNumber(addr.getReceiverPhoneNumber())
                        .build())
                .toList();
    }
}