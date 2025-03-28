package com.fondant.order.domain.entity;

import com.fondant.market.domain.entity.MarketEntity;
import com.fondant.order.presentation.dto.request.OrderItem;
import com.fondant.product.domain.entity.ProductEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "order_detail")
@Getter
public class OrderDetailEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    @JoinColumn(name = "product_id")
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    @JoinColumn(name = "market_id")
    private MarketEntity market;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id")
    private DeliveryEntity delivery;

    @Column(name = "quantity")
    @NotNull
    private int quantity;

    @Column(name = "total_price")
    @NotNull
    private double totalPrice;

    @Builder
    public OrderDetailEntity(
            OrderEntity order,
            ProductEntity product,
            MarketEntity market,
            DeliveryEntity delivery,
            int quantity,
            double totalPrice
    ) {
        this.order = order;
        this.product = product;
        this.market = market;
        this.delivery = delivery;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
    }

    public static OrderDetailEntity from(OrderItem item, OrderEntity order, ProductEntity product, MarketEntity market) {
        double totalPrice = calculateTotalPrice(item.price(), item.optionPrice(), item.quantity(), product.getDiscountRate());

        return OrderDetailEntity.builder()
                .order(order)
                .product(product)
                .market(market)
                .quantity(item.quantity())
                .totalPrice(totalPrice)
                .build();
    }

    private static double calculateTotalPrice(double price, double optionPrice, int quantity, double discountRate) {
        return Math.round((price + optionPrice) * quantity * (1.0 - discountRate));
    }
}