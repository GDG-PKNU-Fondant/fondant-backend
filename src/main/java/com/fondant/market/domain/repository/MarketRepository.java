package com.fondant.market.domain.repository;

import com.fondant.market.domain.entity.MarketEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketRepository extends JpaRepository<MarketEntity, Long>,MarketRepositoryCustom {

}
