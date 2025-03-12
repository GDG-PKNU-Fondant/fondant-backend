package com.fondant.order.domain;

import com.fondant.market.domain.entity.MarketEntity;
import com.fondant.product.domain.entity.OptionEntity;
import com.fondant.product.domain.entity.ProductEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orderDetail")
public class OrderDetailEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id")
    private DeliveryEntity delivery;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    @JoinColumn(name = "market_id")
    private MarketEntity market;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    @JoinColumn(name = "option_id")
    private OptionEntity option;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    @JoinColumn(name = "product_id")
    private ProductEntity product;

    @Column(name = "status")
    @NotNull
    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    @Builder
    public OrderDetailEntity(OrderEntity orderEntity, DeliveryEntity delivery, MarketEntity market, OptionEntity option, ProductEntity product, DeliveryStatus status) {
        this.order = orderEntity;
        this.delivery = delivery;
        this.market = market;
        this.option = option;
        this.product = product;
        this.status = status;
    }
}
