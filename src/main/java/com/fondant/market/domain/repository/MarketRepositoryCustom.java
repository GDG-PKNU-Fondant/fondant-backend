package com.fondant.market.domain.repository;


import com.fondant.market.domain.entity.MarketEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MarketRepositoryCustom {
    Page<MarketEntity> findMarketsByCategory(Long categoryId, Pageable pageable);
    List<MarketEntity> findTop10MarketsByPopularity();
}
