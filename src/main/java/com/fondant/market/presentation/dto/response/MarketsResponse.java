package com.fondant.market.presentation.dto.response;

import com.fondant.global.dto.PageInfo;
import com.fondant.market.application.dto.MarketInfo;
import lombok.Builder;

import java.util.List;

@Builder
public record MarketsResponse(
    PageInfo pageInfo,
    List<MarketInfo> markets
) {
        public static MarketsResponse of(List<MarketInfo> markets,PageInfo pageInfo) {
            return MarketsResponse.builder()
                    .pageInfo(pageInfo)
                    .markets(markets)
                    .build();
        }
}
