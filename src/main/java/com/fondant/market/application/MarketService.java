package com.fondant.market.application;

import com.fondant.global.config.PageConfig;
import com.fondant.global.dto.PageInfo;
import com.fondant.market.application.dto.MarketDetail;
import com.fondant.market.application.dto.MarketInfo;
import com.fondant.market.domain.entity.MarketEntity;
import com.fondant.market.domain.repository.MarketRepository;
import com.fondant.market.presentation.dto.response.MarketsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MarketService {
    private final MarketRepository marketRepository;
    private final PageConfig pageConfig;

    @Transactional(readOnly = true)
    public MarketsResponse getMarketsByCategoryId(Long categoryId, Pageable pageable) {
        Pageable effectivePageable = (pageable == null) ? pageConfig.defaultPageable() : pageable;
        Page<MarketEntity> markets = this.marketRepository.findMarketsByCategory(categoryId, effectivePageable);

        return MarketsResponse.builder()
                .pageInfo(PageInfo.of(markets.getNumber(), markets.getTotalPages()))
                .markets(getMarketInfos(markets.getContent()))
                .build();
    }

    @Transactional(readOnly = true)
    public MarketDetail getMarketById(Long marketId) {
        MarketEntity market = marketRepository.findById(marketId)
                .orElseThrow(() -> new IllegalArgumentException("해당 마켓을 찾을 수 없습니다. ID: " + marketId));

        return convertToDetailDto(market);
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

    private MarketDetail convertToDetailDto(MarketEntity market) {
        return MarketDetail.builder()
                .id(market.getId())
                .name(market.getName())
                .description(market.getDescription())
                .thumbnail(market.getThumbnail())
                .background(market.getBackground())
                .build();
    }
}