package com.fondant.test.repository;

import com.fondant.market.domain.entity.MarketCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketCategoryTestRepository extends JpaRepository<MarketCategoryEntity,Long> {
}
