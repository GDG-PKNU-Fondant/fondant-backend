package com.fondant.order.domain.entity;

import com.fondant.user.domain.entity.UserEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders")
@Getter
public class OrderEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "order_date")
    @NotNull
    private LocalDateTime orderDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id")
    private DeliveryEntity delivery;

    @Column(name = "delivery_address")
    @NotNull
    private String deliveryAddress;

    @Column(name = "total_price")
    @NotNull
    private double totalPrice;

    @Builder
    public OrderEntity(UserEntity user, LocalDateTime orderDate, String deliveryAddress, DeliveryEntity delivery, double totalPrice) {
        this.user = user;
        this.deliveryAddress = deliveryAddress;
        this.delivery = delivery;
        this.orderDate = orderDate;
        this.totalPrice = totalPrice;
    }
}