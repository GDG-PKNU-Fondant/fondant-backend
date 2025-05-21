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

        List<CartMarketInfo> markets = new ArrayList<>();

        for (Map.Entry<Long, List<CartItemEntity>> entry : marketGrouped.entrySet()) {
            Long marketId = entry.getKey();
            List<CartItemEntity> itemsInMarket = entry.getValue();
            String marketName = itemsInMarket.get(0).getCartMarket().getMarket().getName();

            List<CartProductInfo> products = new ArrayList<>();

            for (CartItemEntity item : itemsInMarket) {
                List<CartOptionInfo> optionInfos = Optional.ofNullable(item.getCartItemOptions())
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(opt -> CartOptionInfo.builder()
                                .optionId(opt.getOption().getId())
                                .optionName(opt.getOption().getName())
                                .additionalPrice((double) opt.getOption().getPrice())
                                .quantity(opt.getQuantity())
                                .build())
                        .collect(Collectors.toList());

                int totalQuantity = optionInfos.stream()
                        .mapToInt(CartOptionInfo::quantity)
                        .sum();

                Double totalOptionPrice = optionInfos.stream()
                        .mapToDouble(opt -> opt.additionalPrice() * opt.quantity())
                        .sum();

                Double basePrice = (double) item.getProduct().getPrice();
                Double totalProductPrice = basePrice * totalQuantity + totalOptionPrice;

                Double deliveryFee = Optional.ofNullable(item.getCartMarket().getMarket().getDeliveryFee()).orElse(0.0);
                Double finalPrice = totalProductPrice + deliveryFee;

                CartProductInfo productInfo = CartProductInfo.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .thumbnail(item.getProduct().getThumbnail())
                        .options(optionInfos)
                        .totalQuantity(totalQuantity)
                        .arrivalDate(item.getArrivalDate())
                        .totalProductPrice(totalProductPrice)
                        .deliveryFee(deliveryFee)
                        .finalPrice(finalPrice)
                        .build();

                products.add(productInfo);
            }

            CartMarketInfo marketInfo = CartMarketInfo.builder()
                    .marketId(marketId)
                    .marketName(marketName)
                    .products(products)
                    .build();

            markets.add(marketInfo);
        }

        return CartResponse.of(markets, PageInfo.of(page.getNumber(), page.getTotalPages()));
    }
}