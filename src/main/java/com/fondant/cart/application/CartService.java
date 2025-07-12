package com.fondant.cart.application;

import com.fondant.cart.application.dto.*;
import com.fondant.cart.domain.entity.CartEntity;
import com.fondant.cart.domain.entity.CartItemEntity;
import com.fondant.cart.domain.entity.CartItemOptionEntity;
import com.fondant.cart.domain.entity.CartMarketEntity;
import com.fondant.cart.domain.repository.CartRepository;
import com.fondant.global.config.PageConfig;
import com.fondant.global.dto.PageInfo;
import com.fondant.cart.presentation.dto.response.CartResponse;
import com.fondant.market.domain.entity.MarketEntity;
import com.fondant.product.domain.entity.OptionEntity;
import com.fondant.product.domain.entity.ProductEntity;
import com.fondant.product.domain.repository.OptionRepository;
import com.fondant.product.domain.repository.ProductRepository;
import com.fondant.user.application.UserService;
import com.fondant.user.domain.entity.UserEntity;
import com.fondant.user.domain.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OptionRepository optionRepository;
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

    @Transactional
    public void addCartItem(Long userId, CartInfo request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));

        ProductEntity product = productRepository.findById(request.productId())
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        MarketEntity market = product.getMarket();

        CartEntity cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(CartEntity.builder().user(user).build()));

        CartMarketEntity cartMarket = cart.getCartMarkets().stream()
                .filter(cm -> cm.getMarket().equals(market))
                .findFirst()
                .orElseGet(() -> {
                    CartMarketEntity newMarket = CartMarketEntity.builder()
                            .cart(cart)
                            .market(market)
                            .build();
                    cart.getCartMarkets().add(newMarket);
                    return newMarket;
                });

        CartItemEntity cartItem = CartItemEntity.builder()
                .cartMarket(cartMarket)
                .product(product)
                .quantity(request.quantity())
                .arrivalDate(null)
                .build();

        if (request.options() != null && !request.options().isEmpty()) {
            for (CartInfo.OptionInfo optReq : request.options()) {
                OptionEntity option = optionRepository.findById(optReq.optionId())
                        .orElseThrow(() -> new IllegalArgumentException("옵션이 존재하지 않습니다."));
                CartItemOptionEntity optionEntity = CartItemOptionEntity.builder()
                        .cartItem(cartItem)
                        .option(option)
                        .quantity(optReq.quantity())
                        .build();
                cartItem.addCartItemOption(optionEntity);
            }
        }

        cartMarket.getCartItems().add(cartItem);
        cartRepository.save(cart);
    }

    @Transactional
    public void updateCartItem(Long userId, Long cartItemId, CartUpdateInfo request) {
        CartItemEntity cartItem = cartRepository.findCartItemByIdAndUserId(cartItemId, userId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 상품이 존재하지 않습니다."));

        cartItem.changeQuantity(request.quantity());

        if (request.options() != null && !request.options().isEmpty()) {
            for (CartUpdateInfo.OptionUpdateInfo optionRequest : request.options()) {
                cartItem.getCartItemOptions().stream()
                        .filter(option -> option.getOption().getId().equals(optionRequest.optionId()))
                        .findFirst()
                        .ifPresent(option -> option.changeQuantity(optionRequest.quantity()));
            }
        }
    }
}