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

import java.math.BigDecimal;

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

    @Column(name = "quantity")
    @NotNull
    private int quantity;

    @Column(name = "original_price")
    @NotNull
    private BigDecimal originalPrice;

    @Column(name = "option_price")
    @NotNull
    private BigDecimal optionPrice;

    @Column(name = "discount_rate")
    @NotNull
    private int discountRate;

    @Column(name = "delivery_fee")
    @NotNull
    private BigDecimal deliveryFee;

    @Column(name = "total_price")
    @NotNull
    private BigDecimal totalPrice;

    @Column(name = "product_name")
    @NotNull
    private String productName;

    @Column(name = "thumbnail_url")
    @NotNull
    private String thumbnailUrl;

    @Column(name = "market_name")
    @NotNull
    private String marketName;

    @Column(name = "option_name")
    @NotNull
    private String optionName;

    @Builder
    public OrderDetailEntity(
            OrderEntity order,
            ProductEntity product,
            MarketEntity market,
            int quantity,
            BigDecimal originalPrice,
            BigDecimal optionPrice,
            int discountRate,
            BigDecimal deliveryFee,
            BigDecimal totalPrice,
            String productName,
            String thumbnailUrl,
            String marketName,
            String optionName
            ) {
        this.order = order;
        this.product = product;
        this.market = market;
        this.quantity = quantity;
        this.originalPrice = originalPrice;
        this.optionPrice = optionPrice;
        this.discountRate = discountRate;
        this.deliveryFee = deliveryFee;
        this.totalPrice = totalPrice;
        this.productName = productName;
        this.thumbnailUrl = thumbnailUrl;
        this.marketName = marketName;
        this.optionName = optionName;
    }
}
