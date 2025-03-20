package com.fondant.market.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="market_hashtag")
public class MarketHashtagEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "market_id", nullable = false)
    private MarketEntity market;

    @Column(name = "name", nullable = false, length = 30)
    private String name;

    @Builder
    public MarketHashtagEntity(MarketEntity market, String name) {
        this.market = market;
        this.name = name;
    }
}
