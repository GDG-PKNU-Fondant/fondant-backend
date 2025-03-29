package com.fondant.market.application.dto;
import com.fondant.market.domain.entity.MarketEntity;
import lombok.Builder;

@Builder
public record MarketProfile (
    String businessNumber,
    String instagramProfile,
    String location
) {
    public static MarketProfile from(MarketEntity market) {
        return MarketProfile.builder()
                .businessNumber(market.getBusinessNumber())
                .instagramProfile(market.getInstagramProfile())
                .location(market.getLocation())
                .build();
    }
}