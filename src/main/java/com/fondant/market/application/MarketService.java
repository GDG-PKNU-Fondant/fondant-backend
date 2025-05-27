package com.fondant.market.application;

import com.fondant.global.config.PageConfig;
import com.fondant.global.dto.PageInfo;
import com.fondant.global.exception.ApiException;
import com.fondant.market.application.dto.MarketDetail;
import com.fondant.market.application.dto.MarketInfo;
import com.fondant.market.application.dto.MarketProfile;
import com.fondant.market.domain.entity.MarketEntity;
import com.fondant.market.domain.repository.MarketHashtagRepository;
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
    private final MarketHashtagRepository marketHashtagRepository;
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
    public MarketDetail getMarketById(Long marketId, Long userId) {
        if (marketId == null || marketId <= 0) {
            throw new ApiException(MarketError.INVALID_MARKET_ID);
        }

        MarketEntity market = marketRepository.findById(marketId)
                .orElseThrow(() -> new ApiException(MarketError.MARKET_NOT_FOUND));

        List<String> hashtags = marketHashtagRepository.findNamesByMarketId(marketId);

        boolean liked = marketRepository.isMarketLikedByUser(marketId, userId);
        long likeCount = marketRepository.countLikesByMarket(marketId);

        List<Long> subCategoryIds = marketRepository.findSubCategoryIdsByMarketId(marketId);

        return convertToDetailDto(market, hashtags, liked, likeCount, subCategoryIds);
    }

    @Transactional(readOnly = true)
    public MarketsResponse getTop10MarketsByPopularity(Pageable pageable) {
        Pageable effectivePageable = (pageable == null) ? pageConfig.defaultPageable() : pageable;
        Page<MarketEntity> markets = marketRepository.findTop10MarketsByPopularity(effectivePageable);

        if (markets == null || markets.isEmpty()) {
            throw new ApiException(MarketError.NO_MARKETS_FOUND);
        }

        return MarketsResponse.builder()
                .pageInfo(PageInfo.of(markets.getNumber(), markets.getTotalPages()))
                .markets(getMarketInfos(markets.getContent()))
                .build();
    }

    @Transactional(readOnly = true)
    public MarketsResponse getRandomTop5MarketsByCategoryId(Long categoryId, Pageable pageable) {
        Pageable effectivePageable = (pageable == null) ? pageConfig.defaultPageable() : pageable;
        Page<MarketEntity> markets = marketRepository.findRandomTop5MarketsByCategory(categoryId, effectivePageable);

        if (markets == null || markets.isEmpty()) {
            throw new ApiException(MarketError.NO_MARKETS_FOUND);
        }

        return MarketsResponse.builder()
                .pageInfo(PageInfo.of(markets.getNumber(), markets.getTotalPages()))
                .markets(getMarketInfos(markets.getContent()))
                .build();
    }

    @Transactional(readOnly = true)
    public MarketsResponse getTop30MarketsByCategoryId(Long categoryId, Pageable pageable) {
        if (categoryId == null || categoryId <= 0) {
            throw new ApiException(MarketError.INVALID_CATEGORY_ID);
        }

        Pageable effectivePageable = (pageable == null) ? pageConfig.defaultPageable() : pageable;
        Page<MarketEntity> markets = marketRepository.findTop30MarketsByCategory(categoryId, effectivePageable);

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
                        .totalReviews(market.getTotalReviews())
                        .totalSales(market.getTotalSales())
                        .build())
                .toList();
    }

    private MarketDetail convertToDetailDto(MarketEntity market, List<String> hashtags, boolean liked, long likeCount, List<Long> subCategoryIds) {
        return MarketDetail.builder()
                .id(market.getId())
                .name(market.getName())
                .description(market.getDescription())
                .thumbnail(market.getThumbnail())
                .background(market.getBackground())
                .liked(false)
                .likeCount(likeCount)
                .isTop10(false)
                .hashtags(hashtags)
                .subCategoryIds(subCategoryIds)
                .profile(MarketProfile.builder()
                        .businessNumber(market.getBusinessNumber())
                        .instagramProfile(market.getInstagramProfile())
                        .latitude(market.getLatitude())
                        .longitude(market.getLongitude())
                        .build())
                .build();
    }

    private List<MarketInfo> convertToMarketInfos(List<MarketEntity> markets) {
        return markets.stream()
                .map(market -> MarketInfo.builder()
                        .id(market.getId())
                        .name(market.getName())
                        .description(market.getDescription())
                        .thumbnail(market.getThumbnail())
                        .totalSales(market.getTotalSales())
                        .totalReviews(market.getTotalReviews())
                        .build())
                .toList();
    }
}