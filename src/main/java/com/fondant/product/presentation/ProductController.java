package com.fondant.product.presentation;

import com.fondant.global.dto.ResponseDto;
import com.fondant.global.dto.SuccessMessage;
import com.fondant.product.application.ProductService;
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

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{marketId}/{categoryId}")
    public ResponseEntity<ResponseDto<ProductsResponse>> getProductsByMarketAndCategory(
            @PathVariable(name="marketId") Long marketId,
            @PathVariable(name="categoryId") Long categoryId,
            @RequestParam(name="page") int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);

        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS,
                productService.getProductsByMarketAndCategoryId(marketId,categoryId,pageable)));
    }
}
