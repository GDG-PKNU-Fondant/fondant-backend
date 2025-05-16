package com.fondant.cart.domain.entity;

import com.fondant.product.domain.entity.OptionEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItemOptionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_option_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_item_id")
    private CartItemEntity cartItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id")
    private OptionEntity option;

    @Column(name = "option_quantity", nullable = false)
    private int quantity;

    @Builder
    public CartItemOptionEntity(CartItemEntity cartItem, OptionEntity option, int quantity) {
        this.cartItem = cartItem;
        this.option = option;
        this.quantity = quantity;
    }
}
