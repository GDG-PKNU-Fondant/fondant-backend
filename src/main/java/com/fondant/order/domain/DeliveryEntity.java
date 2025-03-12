package com.fondant.order.domain;

import com.fondant.market.domain.entity.MarketEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "delivery")
public class DeliveryEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    @JoinColumn(name = "market_id")
    private MarketEntity market;

    @OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL)
    private List<OrderDetailEntity> orderDetails = new ArrayList<>();

    @Column(name = "status")
    @NotNull
    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    @Column(name = "status_started_at")
    @NotNull
    private LocalDateTime statusStartedAt;

    @Column(name = "tracking_number")
    @NotNull
    private String trackingNumber;

    @Builder
    public DeliveryEntity(MarketEntity market, DeliveryStatus status, LocalDateTime statusStartedAt, String trackingNumber) {
        this.market = market;
        this.status = status;
        this.statusStartedAt = statusStartedAt;
        this.trackingNumber = trackingNumber;
    }
}
