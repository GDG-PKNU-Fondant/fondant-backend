package com.fondant.product.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product_image")
@Getter
public class ProductImageEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="product_image_id")
    private Long id;

    @Column(name="product_id")
    private Long productId;

    @Column(name="img_url")
    private String imageUrl;

    @Column(name="img_order")
    private Integer imgOrder;

    @Enumerated(EnumType.STRING)
    @Column(name="img_type")
    private  ImageType imageType;

    @Builder
    public ProductImageEntity(Long productId, String imageUrl, ImageType imageType, Integer imgOrder) {
        this.productId = productId;
        this.imageUrl = imageUrl;
        this.imageType = imageType;
        this.imgOrder = imgOrder;
    }
}
