package com.fondant.wishlist.domain.entity;

import com.fondant.product.domain.entity.ProductEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="wishlist")
public class WishListEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="wishlist_id")
    private Long id;

    @Column(name="user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="product_id")
    @Getter
    private ProductEntity product;

    @Builder
    public WishListEntity(Long userId, ProductEntity product) {
        this.userId = userId;
        this.product = product;
    }
}
