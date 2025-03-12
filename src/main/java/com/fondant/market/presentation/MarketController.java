package com.fondant.market.presentation;

import com.fondant.global.config.PageConfig;
import com.fondant.global.dto.ResponseDto;
import com.fondant.global.dto.SuccessMessage;
import com.fondant.market.application.MarketService;
import com.fondant.market.application.dto.MarketDetail;
import com.fondant.market.presentation.dto.response.MarketsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/markets")
public class MarketController {

    private final MarketService marketService;
    private final PageConfig pageConfig;

    @GetMapping("/{marketId}")
    public ResponseEntity<ResponseDto<MarketDetail>> getMarket(@PathVariable long marketId) {
        MarketDetail marketDetail = marketService.getMarketById(marketId);
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS, marketDetail));
    }

    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<ResponseDto<MarketsResponse>> getMarketsByCategoryId(
            @PathVariable(name = "categoryId") Long categoryId,
            @RequestParam(required = false) Pageable pageable) {

        Pageable effectivePageable = (pageable == null) ? pageConfig.defaultPageable() : pageable;

        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS,
                marketService.getMarketsByCategoryId(categoryId, effectivePageable)));
    }

    @GetMapping("/top10")
    public ResponseEntity<ResponseDto<MarketsResponse>> getTop10MarketsByPopularity(
            @RequestParam(required = false) Pageable pageable) {

        Pageable effectivePageable = (pageable == null) ? pageConfig.defaultPageable() : pageable;

        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS,
                marketService.getTop10MarketsByPopularity(effectivePageable)));
    }

    @GetMapping("/top5")
    public ResponseEntity<MarketsResponse> getTop5MarketsByPopularity() {
        return ResponseEntity.ok(marketService.getTop5MarketsByPopularity());
    }

    @GetMapping("/{categoryId}/top30")
    public ResponseEntity<ResponseDto<MarketsResponse>> getTop30MarketsByPopularity(
            @PathVariable(name = "categoryId") Long categoryId,
            @RequestParam(required = false) Pageable pageable) {

        Pageable effectivePageable = (pageable == null) ? pageConfig.defaultPageable() : pageable;
        MarketsResponse marketsResponse = marketService.getTop30MarketsByCategoryId(categoryId, effectivePageable);

        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS, marketsResponse));
    }
}