package com.fondant.market.application;

import com.fondant.global.config.PageConfig;
import com.fondant.global.dto.PageInfo;
import com.fondant.global.exception.ApiException;
import com.fondant.market.application.dto.MarketDetail;
import com.fondant.market.application.dto.MarketInfo;
import com.fondant.market.domain.entity.MarketEntity;
import com.fondant.market.domain.repository.MarketRepository;
import com.fondant.market.exception.MarketError;
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
        if (categoryId == null || categoryId <= 0) {
            throw new ApiException(MarketError.INVALID_CATEGORY_ID);
        }

        Pageable effectivePageable = (pageable == null) ? pageConfig.defaultPageable() : pageable;
        Page<MarketEntity> markets = marketRepository.findMarketsByCategory(categoryId, effectivePageable);

        if (markets.isEmpty()) {
            throw new ApiException(MarketError.NO_MARKETS_FOUND);
        }

        return MarketsResponse.builder()
                .pageInfo(PageInfo.of(markets.getNumber(), markets.getTotalPages()))
                .markets(getMarketInfos(markets.getContent()))
                .build();
    }

    @Transactional(readOnly = true)
    public MarketDetail getMarketById(Long marketId) {
        if (marketId == null || marketId <= 0) {
            throw new ApiException(MarketError.INVALID_MARKET_ID);
        }

        MarketEntity market = marketRepository.findById(marketId)
                .orElseThrow(() -> new ApiException(MarketError.MARKET_NOT_FOUND));

        return convertToDetailDto(market);
    }

    @Transactional(readOnly = true)
    public List<MarketsResponse> getTop10MarketsByPopularity() {
        List<MarketEntity> markets = marketRepository.findTop10MarketsByPopularity();

        if (markets == null || markets.isEmpty()) {
            throw new ApiException(MarketError.NO_MARKETS_FOUND);
        }

        return List.of(MarketsResponse.builder()
                .markets(convertToMarketInfos(markets))
                .build());
    }

    @Transactional(readOnly = true)
    public MarketsResponse getTop5MarketsByPopularity() {
        List<MarketEntity> markets = marketRepository.findTop5MarketsByPopularity();
        if (markets == null || markets.isEmpty()) {
            throw new ApiException(MarketError.NO_MARKETS_FOUND);
        }
        return MarketsResponse.builder()
                .markets(convertToMarketInfos(markets))
                .build();
    }

    @Transactional(readOnly = true)
    public MarketsResponse getTop30MarketsByPopularity() {
        List<MarketEntity> markets = marketRepository.findTop30MarketsByPopularity();
        if (markets == null || markets.isEmpty()) {
            throw new ApiException(MarketError.NO_MARKETS_FOUND);
        }
        return MarketsResponse.builder()
                .markets(convertToMarketInfos(markets))
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

    private MarketDetail convertToDetailDto(MarketEntity market) {
        return MarketDetail.builder()
                .id(market.getId())
                .name(market.getName())
                .description(market.getDescription())
                .thumbnail(market.getThumbnail())
                .background(market.getBackground())
                .build();
    }

    private List<MarketInfo> convertToMarketInfos(List<MarketEntity> markets) {
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