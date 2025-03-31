package com.fondant.market.domain.entity;

import com.fondant.user.domain.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "market_like")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MarketLikeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "market_like_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "market_id", nullable = false)
    private MarketEntity market;

    @Builder public MarketLikeEntity(UserEntity user, MarketEntity market) {
        this.user = user;
    }
}