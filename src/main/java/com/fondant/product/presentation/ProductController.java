package com.fondant.product.presentation;

import com.fondant.global.config.PageConfig;
import com.fondant.global.dto.ResponseDto;
import com.fondant.global.dto.SuccessMessage;
import com.fondant.product.application.ProductService;
import com.fondant.product.presentation.dto.response.ProductDetailResponse;
import com.fondant.product.presentation.dto.response.ProductsResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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
}
