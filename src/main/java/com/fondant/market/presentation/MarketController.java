package com.fondant.market.presentation;

import com.fondant.global.config.PageConfig;
import com.fondant.global.dto.ResponseDto;
import com.fondant.global.dto.SuccessMessage;
import com.fondant.market.application.MarketService;
import com.fondant.market.presentation.dto.response.MarketsResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/market")
public class MarketController {

    private final MarketService marketService;
    private final PageConfig pageConfig;

    public MarketController(MarketService marketService) {
        this.marketService = marketService;
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<ResponseDto<MarketsResponse>> getMarketsByCategoryId(
            @PathVariable(name = "categoryId") Long categoryId,
            @RequestParam(required = false) Pageable pageable) {

        Pageable effectivePageable = (pageable == null) ? pageConfig.defaultPageable() : pageable;

        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS,
                marketService.getMarketsByCategoryId(categoryId, effectivePageable)));
    }
}
