package com.fondant.order.application;

import com.fondant.coupon.domain.entity.CouponEntity;
import com.fondant.coupon.domain.entity.UserCouponEntity;
import com.fondant.coupon.domain.repository.UserCouponRepository;
import com.fondant.coupon.util.CouponUtil;
import com.fondant.global.exception.ApiException;
import com.fondant.market.domain.entity.MarketEntity;
import com.fondant.order.domain.entity.OrderDetailEntity;
import com.fondant.order.domain.entity.OrderEntity;
import com.fondant.order.domain.entity.OrderStatus;
import com.fondant.order.domain.repository.OrderDetailRepository;
import com.fondant.order.domain.repository.OrderRepository;
import com.fondant.order.exception.OrderError;
import com.fondant.order.presentation.OrderResponse;
import com.fondant.order.presentation.dto.CheckoutItem;
import com.fondant.order.presentation.dto.CouponApplyDto;
import com.fondant.order.presentation.dto.CouponValidationResult;
import com.fondant.order.presentation.dto.request.*;
import com.fondant.order.presentation.dto.response.OrderPrepareResponse;
import com.fondant.product.application.ProductService;
import com.fondant.product.domain.entity.OptionEntity;
import com.fondant.product.domain.entity.ProductEntity;
import com.fondant.product.domain.repository.ProductRepository;
import com.fondant.product.exception.ProductError;
import com.fondant.product.util.ProductUtil;
import com.fondant.user.application.UserService;
import com.fondant.user.application.dto.CustomUserDetails;
import com.fondant.user.domain.entity.UserEntity;
import com.fondant.user.exception.UserError;
import com.fondant.coupon.application.CouponService;
import com.fondant.coupon.application.dto.CouponInfo;
import com.fondant.coupon.presentation.dto.response.CouponListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final ProductRepository productRepository;
    private final ProductService productService;
    private final CouponService couponService;
    private final UserService userService;
    private final UserCouponRepository userCouponRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductUtil productUtil;
    private final CouponUtil couponUtil;

    @Transactional
    public OrderPrepareResponse prepareOrder(Long userId, OrderPrepareRequest request) {
        UserEntity user = userService.getUserEntityById(userId);

        validateStockForPrepare(request.items());

        List<OrderPrepareResponse.PreparedOrderItemDto> preparedItems = new ArrayList<>();
        double totalOrderPrice = 0.0;

        for (CheckoutItem item : request.items()) {
            OptionEntity option = productService.findOptionByIdAndProductId(item.optionId(), item.productId());
            ProductEntity product = productService.findProductById(item.productId());

            Double discountedPrice = productService.getDiscountedPrice(product.getPrice() + option.getPrice(), product.getDiscountRate());

            totalOrderPrice += discountedPrice * item.quantity();

            preparedItems.add(toPreparedOrderItemDto(product, option, item, discountedPrice));
        }

        CouponListResponse couponResponse = couponService.getIssuedCoupons(userId, Pageable.unpaged());
        List<CouponInfo> availableCoupons = couponResponse.coupons();

        int userPoint = user.getPoint();

        List<OrderPrepareResponse.DeliveryAddressDto> deliveryAddresses = toDeliveryAddressDtos(user);

        return OrderPrepareResponse.builder()
                .orderItems(preparedItems)
                .totalOrderPrice(totalOrderPrice)
                .deliveryAddresses(deliveryAddresses)
                .availableCoupons(availableCoupons)
                .point(userPoint)
                .build();
    }

    private void validateStockForPrepare(List<CheckoutItem> items) {
        for (CheckoutItem item : items) {
            ProductEntity product = productService.findProductById(item.productId());
            if (product.getMaxCount() < item.quantity()) {
                throw new ApiException(OrderError.OUT_OF_STOCK);
            }
        }
    }

    @Transactional
    public OrderResponse createOrder(CustomUserDetails userDetails, OrderRequest request) {
        UserEntity userEntity = userService.getUserEntityById(userDetails.getUserId());
        Integer expected = validateAllCondition(userEntity, request);
        Long orderId = saveAllOrder(userEntity, request, expected);

        log.info("주문 검증 완료 - 사용자: {}, 예상 금액: {}", userEntity.getPhoneNumber(), expected);
        return new OrderResponse(orderId);
    }

    @Transactional
    public void rollbackOrderAndStock(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId).orElseThrow(() -> new ApiException(OrderError.ORDER_NOT_FOUND));
        List<OrderDetailEntity> orderDetails = orderDetailRepository.findAllByOrder(order);

        for (OrderDetailEntity detail : orderDetails) {
            productUtil.rollbackReservedStockWithLock(detail.getProduct().getId(),detail.getQuantity());
        }
    }


    private Long saveAllOrder(UserEntity userEntity, OrderRequest request, Integer expected) {
        OrderEntity order = OrderEntity.builder()
                .user(userEntity)
                .orderDate(LocalDateTime.now())
                .delivery(null)
                .deliveryAddress(userService.findDeliveryAddressById(userEntity.getId(),
                        request.deliveryAddressId()).getDeliveryAddress())
                .totalPrice(Double.valueOf(request.expectedAmount()))
                .status(OrderStatus.RESERVED)
                .build();
        orderRepository.save(order);

        for (CheckoutItem item : request.checkoutItems()) {
            ProductEntity product = productService.findProductById(item.productId());
            OrderDetailEntity orderDetail = OrderDetailEntity.builder()
                    .order(order)
                    .product(product)
                    .market(product.getMarket())
                    .delivery(null)
                    .quantity(item.quantity())
                    .totalPrice(expected)
                    .build();
            orderDetailRepository.save(orderDetail);
            productUtil.reserveStock(item.productId(), item.quantity());
        }
        return order.getId();
    }

    private Integer validateAllCondition(UserEntity userEntity, OrderRequest request) {
        Map<Long, CheckoutItem> itemMap = validateStock(request.checkoutItems());
        Map<MarketEntity, List<CheckoutItem>> itemsByMarket = divideCheckoutItemsByMarket(itemMap);

        PriceCalculationResult priceResult = calculateProductTotalAndDeliveryFee(itemsByMarket);
        double productTotal = priceResult.productTotal();
        double deliveryFee = priceResult.deliveryFee();

        CouponValidationResult couponResult = validateCoupons(userEntity.getId(), request.couponAllies(), itemMap);
        double couponDiscount = couponResult.totalDiscount();

        if (request.discountPoints() > userEntity.getPoint()) {
            throw new ApiException(UserError.INVALID_POINT);
        }

        int expected = (int) (productTotal + deliveryFee - couponDiscount - request.discountPoints());
        if (expected != request.expectedAmount()) {
            throw new ApiException(OrderError.INVALID_AMOUNT);
        }
        return expected;
    }

    private PriceCalculationResult calculateProductTotalAndDeliveryFee(Map<MarketEntity, List<CheckoutItem>> itemsByMarket) {
        double productTotal = 0.0;
        double deliveryFee = 0.0;

        for (Map.Entry<MarketEntity, List<CheckoutItem>> entry : itemsByMarket.entrySet()) {
            MarketEntity market = entry.getKey();
            List<CheckoutItem> items = entry.getValue();

            double marketTotal = items.stream()
                    .mapToDouble(item -> {
                        ProductEntity product = productRepository.getReferenceById(item.productId());
                        return product.getPrice() * item.quantity();
                    })
                    .sum();
            productTotal += marketTotal;

            double marketDeliveryFee = (marketTotal >= market.getFreeDeliveryLimit()) ? 0.0 : market.getDeliveryFee();
            deliveryFee += marketDeliveryFee;
        }

        return new PriceCalculationResult(productTotal, deliveryFee);
    }

    private record PriceCalculationResult(Double productTotal, Double deliveryFee) {
    }

    private Map<Long, CheckoutItem> validateStock(List<CheckoutItem> items) {
        Set<Long> productIds = items.stream()
                .map(CheckoutItem::productId)
                .collect(Collectors.toSet());
        List<ProductEntity> products = productRepository.findAllById(productIds);

        Map<Long, ProductEntity> productMap = products.stream()
                .collect(Collectors.toMap(ProductEntity::getId, p -> p));

        Map<Long, CheckoutItem> result = new HashMap<>();
        for (int i = 0; i < items.size(); i++) {
            CheckoutItem item = items.get(i);
            ProductEntity product = productMap.get(item.productId());
            if (product == null) {
                throw new ApiException(ProductError.PRODUCT_NOT_FOUND);
            }
            if (product.getMaxCount() < item.quantity()) {
                throw new ApiException(OrderError.OUT_OF_STOCK);
            }
            result.put((long) i, item);
        }
        return result;
    }

    public CouponValidationResult validateCoupons(
            Long userId,
            List<CouponApplyDto> applies,
            Map<Long, CheckoutItem> itemMap) {

        if (applies == null || applies.isEmpty())
            return new CouponValidationResult();

        Set<Long> usedCouponIds = new HashSet<>();
        Set<Long> discountedItemIds = new HashSet<>();
        Map<Long, Long> couponToItem = new HashMap<>();
        int totalDiscount = 0;

        Set<Long> couponIds = applies.stream()
                .map(CouponApplyDto::couponId)
                .collect(Collectors.toSet());

        List<UserCouponEntity> userCoupons = userCouponRepository.findValidUserCoupons(userId, couponIds);

        Map<Long, UserCouponEntity> userCouponMap = userCoupons.stream()
                .collect(Collectors.toMap(
                        userCoupon -> userCoupon.getCoupon().getId(),
                        userCoupon -> userCoupon
                ));

        for (CouponApplyDto apply : applies) {
            if (!usedCouponIds.add(apply.couponId()))
                throw new ApiException(OrderError.DUPLICATE_COUPON);
            if (!discountedItemIds.add(apply.targetItemId()))
                throw new ApiException(OrderError.DUPLICATE_COUPON_ITEM);

            UserCouponEntity userCoupon = userCouponMap.get(apply.couponId());
            if (userCoupon == null)
                throw new ApiException(OrderError.INVALID_COUPON);

            CouponEntity coupon = userCoupon.getCoupon();

            CheckoutItem checkoutItem = itemMap.get(apply.targetItemId());
            if (checkoutItem == null)
                throw new ApiException(OrderError.COUPON_ITEM_NOT_MATCH);

            ProductEntity product = productRepository.getReferenceById(checkoutItem.productId());
            if (!coupon.isGlobal() && !product.getMarket().getId().equals(coupon.getCouponMarkets().get(0).getMarket().getId()))
                throw new ApiException(OrderError.INVALID_COUPON_SCOPE);

            int subTotal = (int) (product.getPrice() * checkoutItem.quantity());
            if (subTotal < coupon.getMinOrderAmount())
                throw new ApiException(OrderError.MIN_PRICE_NOT_MET);

            totalDiscount += couponUtil.calcDiscount(coupon, subTotal);

            couponToItem.put(coupon.getId(), apply.targetItemId());
        }

        return new CouponValidationResult(totalDiscount, usedCouponIds, couponToItem);
    }

    private OrderPrepareResponse.PreparedOrderItemDto toPreparedOrderItemDto(
            ProductEntity product, OptionEntity option, CheckoutItem item, double discountedPrice) {

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


    private Map<MarketEntity, List<CheckoutItem>> divideCheckoutItemsByMarket(Map<Long, CheckoutItem> itemMap) {
        Map<MarketEntity, List<CheckoutItem>> itemsByMarket = new HashMap<>();

        for (CheckoutItem item : itemMap.values()) {
            ProductEntity product = productRepository.getReferenceById(item.productId());
            MarketEntity market = product.getMarket();

            itemsByMarket.computeIfAbsent(market, k -> new ArrayList<>()).add(item);
        }

        return itemsByMarket;
    }
}