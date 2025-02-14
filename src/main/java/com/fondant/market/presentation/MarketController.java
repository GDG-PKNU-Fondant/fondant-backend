package com.fondant.market.presentation;

import com.fondant.global.config.PageConfig;
import com.fondant.global.dto.ResponseDto;
import com.fondant.global.dto.SuccessMessage;
import com.fondant.market.application.MarketService;
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
    public ResponseEntity<Object> getMarket(@PathVariable long marketId) {
        return ResponseEntity.ok(null);
    }

    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<ResponseDto<MarketsResponse>> getMarketsByCategoryId(
            @PathVariable(name = "categoryId") Long categoryId,
            @RequestParam(required = false) Pageable pageable) {

        Pageable effectivePageable = (pageable == null) ? pageConfig.defaultPageable() : pageable;

        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS,
                marketService.getMarketsByCategoryId(categoryId, effectivePageable)));
    }
}