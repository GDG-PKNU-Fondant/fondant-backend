package com.fondant.product.presentation;

import com.fondant.global.config.PageConfig;
import com.fondant.global.dto.ResponseDto;
import com.fondant.global.dto.SuccessMessage;
import com.fondant.product.application.ProductService;
import com.fondant.product.application.dto.FilterInfo;
import com.fondant.product.presentation.dto.response.FilteredProductCountResponse;
import com.fondant.product.presentation.dto.response.ProductDetailResponse;
import com.fondant.product.presentation.dto.response.ProductsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/product")
public class ProductController {
    private static int PAGE_SIZE = 10;

    private final ProductService productService;
    private final PageConfig pageConfig;

    public ProductController(ProductService productService, PageConfig pageConfig) {
        this.productService = productService;
        this.pageConfig = pageConfig;
    }

    @GetMapping("/{marketId}/{categoryId}")
    public ResponseEntity<ResponseDto<ProductsResponse>> getProductsByMarketAndCategory(
            @PathVariable(name="marketId") Long marketId,
            @PathVariable(name="categoryId") Long categoryId,
            @RequestParam(name="page") int page) {
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS,
                productService.getProductsByMarketAndCategoryId(marketId,categoryId, pageConfig.defaultPageable())));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ResponseDto<ProductDetailResponse>> getProductDetail(
            @PathVariable(name="productId") Long productId) {
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS,
                productService.getProductDetail(productId)));
    }

    @GetMapping("/filter/count")
    public ResponseEntity<ResponseDto<FilteredProductCountResponse>> getFilterCount(
            @RequestParam Optional<Double> minPrice,
            @RequestParam Optional<Double> maxPrice,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) List<String> packagingTypes,
            @RequestParam(required = false) List<String> benefits) {
        FilterInfo filterinfo = new FilterInfo(minPrice,maxPrice,categoryIds,packagingTypes,benefits);

        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS,
                productService.getFilteredProductCounts(filterinfo)));
    }
}
