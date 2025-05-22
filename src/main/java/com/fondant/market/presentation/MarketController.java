package com.fondant.market.presentation;

import com.fondant.global.annotation.CurrentUser;
import com.fondant.global.config.PageConfig;
import com.fondant.global.dto.ResponseDto;
import com.fondant.global.dto.SuccessMessage;
import com.fondant.market.application.MarketService;
import com.fondant.market.application.dto.MarketDetail;
import com.fondant.market.presentation.dto.response.MarketsResponse;
import com.fondant.user.application.dto.CustomUserDetails;
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
    public ResponseEntity<ResponseDto<MarketDetail>> getMarket(@PathVariable long marketId, @CurrentUser CustomUserDetails user) {
        MarketDetail marketDetail = marketService.getMarketById(marketId, user.getUserId());
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

    @GetMapping("/{categoryId}/top5")
    public ResponseEntity<ResponseDto<MarketsResponse>> getRandomTop5MarketsByCategoryId(
            @PathVariable(name = "categoryId") Long categoryId,
            @RequestParam(required = false) Pageable pageable) {

        Pageable effectivePageable = (pageable == null) ? pageConfig.defaultPageable() : pageable;
        MarketsResponse marketsResponse = marketService.getRandomTop5MarketsByCategoryId(categoryId, effectivePageable);

        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS, marketsResponse));
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