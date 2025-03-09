package com.fondant.market.application.dto;

import com.fondant.market.domain.entity.MarketEntity;
import lombok.Builder;

@Builder
public record MarketDetail(
        Long id,
        String name,
        String description,
        String thumbnail,
        String background
) {
    public static MarketDetail of(MarketEntity market) {
        return MarketDetail.builder()
                .id(market.getId())
                .name(market.getName())
                .description(market.getDescription())
                .thumbnail(market.getThumbnail())
                .background(market.getBackground())
                .build();
    }
}