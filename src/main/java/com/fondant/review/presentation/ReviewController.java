package com.fondant.review.presentation;

import com.fondant.global.annotation.CurrentUser;
import com.fondant.global.dto.ResponseDto;
import com.fondant.global.dto.SuccessMessage;
import com.fondant.product.presentation.dto.response.ProductsResponse;
import com.fondant.review.application.ReviewService;
import com.fondant.review.presentation.dto.request.ReviewCreateRequest;
import com.fondant.review.presentation.dto.request.ReviewUpdateRequest;
import com.fondant.user.application.dto.CustomUserDetails;
import org.springframework.http.MediaType;
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

    @PostMapping(value = "/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDto<Void>> createReview(
            @CurrentUser CustomUserDetails user,
            @PathVariable(name="productId") Long productId,
            @RequestPart("data")ReviewCreateRequest request,
            @RequestPart(value="files",required = false) List<MultipartFile> photos) {
        reviewService.createReview(photos,request,user.getUserId(),productId);
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.CREATE_SUCCESS));
    }

    @PatchMapping(value = "/{reviewId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDto<Void>> updateReview(
            @CurrentUser CustomUserDetails user,
            @PathVariable Long reviewId,
            @RequestPart("data") ReviewUpdateRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> newPhotos
    ) {
        reviewService.updateReview(reviewId, request, newPhotos);
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.UPDATE_SUCCESS));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ResponseDto<Void>> deleteReview(
            @CurrentUser CustomUserDetails user,
            @PathVariable Long reviewId
    ) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.DELETE_SUCCESS));
    }

}
