package com.fondant.market.application.dto;

import com.fondant.market.domain.entity.MarketEntity;
import lombok.Builder;

import java.util.List;

@Builder
public record MarketDetail(
        Long id,
        String name,
        String description,
        String thumbnail,
        String background,
        boolean liked,
        Long likeCount,
        boolean isTop10,
        List<String> hashtags,
        List<Long> subCategoryIds,
        MarketProfile profile
) {
    public static MarketDetail of(
            MarketEntity market,
            boolean liked,
            Long likeCount,
            boolean isTop10,
            List<String> hashtags,
            List<Long> subCategoryIds
    ){
        return new MarketDetail(
                market.getId(),
                market.getName(),
                market.getDescription(),
                market.getThumbnail(),
                market.getBackground(),
                liked,
                likeCount != null ? likeCount : 0L,
                isTop10,
                hashtags,
                subCategoryIds,
                MarketProfile.from(market)
        );
    }
}