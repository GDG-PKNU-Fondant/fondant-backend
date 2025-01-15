package com.fondant.product.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="category")
public class CategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="category_id")
    private Long id;

    @Column(name="category_type",nullable = false)
    @Enumerated(EnumType.STRING)
    private CategoryType categoryType;

    @Column(name="product_id",nullable = false)
    private Long productId;

    @Builder
    public CategoryEntity(CategoryType categoryType, Long productId) {
        this.categoryType = categoryType;
        this.productId = productId;
    }
}
