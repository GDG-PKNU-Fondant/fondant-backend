package com.fondant.market.domain.repository;

import com.fondant.market.domain.entity.MarketHashtagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MarketHashtagRepository extends JpaRepository<MarketHashtagEntity, Long> {

    @Query("SELECT h.name FROM MarketHashtagEntity h WHERE h.market.id = :marketId")
    List<String> findNamesByMarketId(@Param("marketId") Long marketId);
}
