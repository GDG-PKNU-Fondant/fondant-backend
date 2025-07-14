package com.fondant.cart.domain.entity;

import com.fondant.product.domain.entity.ProductEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="cart_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_market_id")
    private CartMarketEntity cartMarket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private ProductEntity product;

    @Column(name="quantity", nullable = false)
    private int quantity;

    @Column(name="arrival_date", nullable = false)
    private LocalDate arrivalDate;

    @OneToMany(mappedBy = "cartItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItemOptionEntity> cartItemOptions = new ArrayList<>();

    @Builder
    public CartItemEntity(CartMarketEntity cartMarket, ProductEntity product, int quantity,
                          LocalDate arrivalDate, List<CartItemOptionEntity> cartItemOptions) {
        this.cartMarket = cartMarket;
        this.product = product;
        this.quantity = quantity;
        this.arrivalDate = (arrivalDate != null) ? arrivalDate : LocalDate.now().plusDays(3);
        if (cartItemOptions != null) {
            cartItemOptions.forEach(this::addCartItemOption);
        }
    }
    public void addCartItemOption(CartItemOptionEntity option) {
        this.cartItemOptions.add(option);
        option.setCartItem(this);
    }

    public void changeQuantity(int quantity) {
        this.quantity = quantity;
    }
}
