package com.fondant.cart.application;

import com.fondant.cart.application.dto.CartMarketInfo;
import com.fondant.cart.application.dto.CartOptionInfo;
import com.fondant.cart.application.dto.CartProductInfo;
import com.fondant.cart.domain.entity.CartItemEntity;
import com.fondant.cart.domain.repository.CartRepository;
import com.fondant.global.config.PageConfig;
import com.fondant.global.dto.PageInfo;
import com.fondant.cart.presentation.dto.response.CartResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final PageConfig pageConfig;

    @Transactional(readOnly = true)
    public CartResponse getCartItemsByUser(Long userId, Pageable pageable) {
        Pageable effectivePageable = (pageable == null) ? pageConfig.defaultPageable() : pageable;
        Page<CartItemEntity> page = cartRepository.findCartItemsByUser(userId, effectivePageable);
        List<CartItemEntity> cartItems = page.getContent();

        Map<Long, List<CartItemEntity>> marketGrouped = cartItems.stream()
                .collect(Collectors.groupingBy(item -> item.getCartMarket().getMarket().getId()));

        List<CartMarketInfo> markets = toCartMarketInfoList(marketGrouped);

        return CartResponse.of(markets, PageInfo.of(page.getNumber(), page.getTotalPages()));
    }

    private List<CartMarketInfo> toCartMarketInfoList(Map<Long, List<CartItemEntity>> marketGrouped) {
        return marketGrouped.entrySet().stream()
                .map(entry -> {
                    Long marketId = entry.getKey();
                    List<CartItemEntity> items = entry.getValue();
                    var market = items.get(0).getCartMarket().getMarket();

                    List<CartProductInfo> products = toCartProductInfoList(items);

                    return CartMarketInfo.builder()
                            .marketId(marketId)
                            .marketName(market.getName())
                            .freeDeliveryLimit(market.getFreeDeliveryLimit())
                            .products(products)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<CartProductInfo> toCartProductInfoList(List<CartItemEntity> items) {
        return items.stream()
                .map(item -> {
                    List<CartOptionInfo> options = Optional.ofNullable(item.getCartItemOptions())
                            .orElse(Collections.emptyList())
                            .stream()
                            .map(opt -> CartOptionInfo.builder()
                                    .optionId(opt.getOption().getId())
                                    .optionName(opt.getOption().getName())
                                    .additionalPrice((double) opt.getOption().getPrice())
                                    .quantity(opt.getQuantity())
                                    .build())
                            .collect(Collectors.toList());

                    return CartProductInfo.builder()
                            .productId(item.getProduct().getId())
                            .productName(item.getProduct().getName())
                            .thumbnail(item.getProduct().getThumbnail())
                            .basePrice(item.getProduct().getPrice())
                            .options(options)
                            .quantity(item.getQuantity())
                            .arrivalDate(item.getArrivalDate())
                            .build();
                })
                .collect(Collectors.toList());
    }
}