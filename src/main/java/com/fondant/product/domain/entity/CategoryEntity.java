package com.fondant.product.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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

    @NotNull
    @Column(name="category_type")
    @Enumerated(EnumType.STRING)
    private CategoryType categoryType;

    @NotNull
    @Column(name="product_id")
    private Long productId;

    @Builder
    public CategoryEntity(CategoryType categoryType, Long productId) {
        this.categoryType = categoryType;
        this.productId = productId;
    }
}
