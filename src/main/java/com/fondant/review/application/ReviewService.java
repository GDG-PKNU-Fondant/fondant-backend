package com.fondant.review.application;

import com.fondant.product.application.ProductService;
import com.fondant.product.domain.entity.ProductEntity;
import com.fondant.review.domain.entity.ReviewEntity;
import com.fondant.review.domain.repository.ReviewRepository;
import com.fondant.review.presentation.dto.request.ReviewCreateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ProductService productService;

    public ReviewService(ReviewRepository reviewRepository, ProductService productService) {
        this.reviewRepository = reviewRepository;
        this.productService = productService;
    }

    @Transactional
    public void createReview (List<MultipartFile> photoFiles, ReviewCreateRequest request, Long userId, Long productId)
    {
        savePhotos(photoFiles);

        reviewRepository.save(
                ReviewEntity.builder()
                        .score(request.score())
                        .userId(userId)
                        .content(request.content())
                        .productEntity(productService.getProductById(productId))
                        .build()
        );
    }

    public void savePhotos(List<MultipartFile> photoFiles){
        /*S3연결 및 이미지 url저장 필요*/
    }
}
