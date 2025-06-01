package com.fondant.review.presentation;

import com.fondant.global.annotation.CurrentUser;
import com.fondant.global.dto.ResponseDto;
import com.fondant.global.dto.SuccessMessage;
import com.fondant.product.presentation.dto.response.ProductsResponse;
import com.fondant.review.application.ReviewService;
import com.fondant.review.presentation.dto.request.ReviewCreateRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/{productId}")
    public ResponseEntity<ResponseDto<Void>> createReview(
            @CurrentUser Long userId,
            @PathVariable(name="productId") Long productId,
            @RequestPart("data")ReviewCreateRequest request,
            @RequestPart(value="files",required = false) List<MultipartFile> photos) {
        reviewService.createReview(photos,request,userId,productId);
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.CREATE_SUCCESS));

    }
}
