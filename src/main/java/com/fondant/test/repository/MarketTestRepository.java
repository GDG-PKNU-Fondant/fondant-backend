package com.fondant.test.repository;

import com.fondant.market.domain.entity.MarketEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketTestRepository extends JpaRepository<MarketEntity,Long> {
}
