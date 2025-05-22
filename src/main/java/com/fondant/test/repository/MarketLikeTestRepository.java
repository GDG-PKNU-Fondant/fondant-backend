package com.fondant.test.repository;

import com.fondant.market.domain.entity.MarketLikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketLikeTestRepository extends JpaRepository<MarketLikeEntity,Long> {
}
