package com.fondant.market.application;

import com.fondant.global.dto.PageInfo;
import com.fondant.market.application.dto.MarketInfo;
import com.fondant.market.domain.entity.MarketEntity;
import com.fondant.market.domain.repository.MarketRepository;
import com.fondant.market.presentation.dto.response.MarketsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MarketService {
    private final MarketRepository marketRepository;

    @Autowired
    public MarketService(MarketRepository marketRepository) {
        this.marketRepository = marketRepository;
    }

    @Transactional(readOnly = true)
    public MarketsResponse getMarketsByCategoryId(Long categoryId, Pageable pageable) {
        Page<MarketEntity> markets = marketRepository.findMarketsByCategory(categoryId,pageable);

        return MarketsResponse.builder()
                .pageInfo(PageInfo.of(markets.getNumber(), markets.getTotalPages()))
                .markets(getMarketInfos(markets.getContent()))
                .build();
    }

    private List<MarketInfo> getMarketInfos(List<MarketEntity> markets) {
        return markets.stream()
                .map(market -> MarketInfo.builder()
                        .id(market.getId())
                        .name(market.getName())
                        .description(market.getDescription())
                        .thumbnail(market.getThumbnail())
                        .build())
                .toList();
    }
}