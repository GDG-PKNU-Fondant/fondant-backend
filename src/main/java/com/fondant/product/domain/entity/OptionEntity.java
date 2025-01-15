package com.fondant.product.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="option")
public class OptionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="option_id")
    private Long id;

    @Column(name="productId",nullable = false)
    private Long productId;

    @Column(name="name",nullable = false)
    private String name;

    @Column(name="price",nullable = false)
    private int price;

    @Builder
    public OptionEntity(Long productId, String name, int price) {
        this.productId = productId;
        this.name = name;
        this.price = price;
    }
}
