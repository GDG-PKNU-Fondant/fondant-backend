package com.fondant.market.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@ToString
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="market")
public class MarketEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="market_id")
    private Long id;

    @NotNull
    @Column(name="market_name", nullable = false)
    private String name;

    @NotNull
    @Column(name="total_sales", nullable = false)
    private Long totalSales;

    @NotNull
    @Column(name="total_reviews", nullable = false)
    private Long totalReviews;

    @NotNull
    @Column(name="create_at", nullable = false)
    private LocalDate createAt;

    @Column(name="update_at")
    private LocalDate updateAt;

    @Column(name="description")
    private String description;

    @NotNull
    @Column(name="thumbnail")
    private String thumbnail;

    @NotNull
    @Column(name="background")
    private String background;

    @NotNull
    @Column(name="delivery_fee", nullable = false)
    private Double deliveryFee;

    @NotNull
    @Column(name="free_delivery_limit", nullable = false)
    private Double freeDeliveryLimit;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "address")
    private String address;

    @Column(name = "business_number")
    private String businessNumber;

    @Column(name = "naver_link")
    private String naverLink;

    @Column(name = "instagram_profile")
    private String instagramProfile;

    @Builder
    public MarketEntity(String name, Long totalSales, Long totalReviews, LocalDate createAt, LocalDate updateAt,
                        String description, String thumbnail, String background,
                        Double deliveryFee, Double freeDeliveryLimit,
                        Double latitude, Double longitude, String address,
                        String businessNumber, String naverLink, String instagramProfile) {
        this.name = name;
        this.totalSales = totalSales != null ? totalSales : 0L;
        this.totalReviews = totalReviews != null ? totalReviews : 0L;
        this.createAt = createAt != null ? createAt : LocalDate.now();
        this.updateAt = updateAt;
        this.description = description;
        this.thumbnail = thumbnail;
        this.background = background;
        this.deliveryFee = deliveryFee != null ? deliveryFee : 0L;
        this.freeDeliveryLimit = freeDeliveryLimit != null ? freeDeliveryLimit : 0L;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.businessNumber = businessNumber;
        this.naverLink = naverLink;
        this.instagramProfile = instagramProfile;
    }

}