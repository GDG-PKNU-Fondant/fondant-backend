package com.fondant.order.domain;


import com.fondant.user.domain.entity.DeliveryAddressEntity;
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
@Table(name = "order")
public class OrderEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "order_date")
    @NotNull
    private LocalDateTime orderDate;

    @Column(name = "order_date")
    @NotNull
    private String deliveryAddress;

    @Builder
    public OrderEntity(UserEntity user, LocalDateTime orderDate, String deliveryAddress) {
        this.user = user;
        this.deliveryAddress = deliveryAddress;
        this.orderDate = orderDate;
    }
}
