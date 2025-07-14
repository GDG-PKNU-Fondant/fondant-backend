package com.fondant.review.application;

import com.fondant.global.dto.PageInfo;
import com.fondant.infra.s3.application.S3Service;
import com.fondant.product.application.ProductService;
import com.fondant.review.domain.entity.ReviewEntity;
import com.fondant.review.domain.entity.ReviewPhotoEntity;
import com.fondant.review.domain.repository.ReviewPhotoRepository;
import com.fondant.review.domain.repository.ReviewRepository;
import com.fondant.review.domain.repository.ReviewTagRepository;
import com.fondant.review.presentation.dto.info.ReviewInfo;
import com.fondant.review.presentation.dto.info.TagInfo;
import com.fondant.review.presentation.dto.request.ReviewCreateRequest;
import com.fondant.review.presentation.dto.response.ReviewsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewTagRepository reviewTagRepository;
    private final ReviewPhotoRepository reviewPhotoRepository;
    private final S3Service s3Service;

    public ReviewService(ReviewRepository reviewRepository, ProductService productService, ReviewTagRepository reviewTagRepository, ReviewPhotoRepository reviewPhotoRepository, S3Service s3Service) {
        this.reviewRepository = reviewRepository;
        this.reviewTagRepository = reviewTagRepository;
        this.reviewPhotoRepository = reviewPhotoRepository;
        this.s3Service = s3Service;
    }

    @Transactional
    public void createReview (List<MultipartFile> photoFiles, ReviewCreateRequest request, Long userId, Long productId)
    {
        ReviewEntity review = reviewRepository.save(
                ReviewEntity.builder()
                        .score(request.score())
                        .userId(userId)
                        .content(request.content())
                        .productId(productId)
                        .build()
        );

        savePhotos(photoFiles,review.getId());
    }

    public void savePhotos(List<MultipartFile> photoFiles,Long reviewId){
        if (photoFiles == null || photoFiles.isEmpty()) return;

        List<String> uploadedUrls = photoFiles.stream()
                .map(s3Service::uploadReviewImage)
                .toList();

        List<ReviewPhotoEntity> photos = uploadedUrls.stream()
                .map(url -> ReviewPhotoEntity.builder()
                        .reviewId(reviewId)
                        .imageUrl(url)
                        .build())
                .toList();

        reviewPhotoRepository.saveAll(photos);
    }

    @Transactional(readOnly = true)
    public ReviewsResponse getReviews(Long productId, Pageable pageable) {
        Page<ReviewEntity> reviewPage = reviewRepository.findByProductId(productId, pageable);

        List<Long> reviewIds = extractReviewIds(reviewPage);

        Map<Long, List<String>> imageMap = getImageMapByReviewIds(reviewIds);
        Map<Long, List<TagInfo>> tagMap = getTagMapByReviewIds(reviewIds);

        List<ReviewInfo> reviewInfos = toReviewInfoList(reviewPage.getContent(), imageMap, tagMap);

        return new ReviewsResponse(PageInfo.of(reviewPage.getNumber(),reviewPage.getTotalPages()),productId, reviewInfos);
    }

    public Map<Long, List<String>> getImageMapByReviewIds(List<Long> reviewIds) {
        return reviewPhotoRepository.findByReviewIdIn(reviewIds).stream()
                .collect(Collectors.groupingBy(
                        ReviewPhotoEntity::getReviewId,
                        Collectors.mapping(ReviewPhotoEntity::getImageUrl, Collectors.toList())
                ));
    }

    public Map<Long, List<TagInfo>> getTagMapByReviewIds(List<Long> reviewIds) {
        return reviewTagRepository.findByReviewIdIn(reviewIds).stream()
                .collect(Collectors.groupingBy(
                        tag -> tag.getReview().getId(),
                        Collectors.mapping(tag ->
                                        new TagInfo(tag.getHashtag().getCategory(), tag.getHashtag().getContent()),
                                Collectors.toList())
                ));
    }

    public List<ReviewInfo> toReviewInfoList(List<ReviewEntity> reviews,
                                             Map<Long, List<String>> imageMap,
                                             Map<Long, List<TagInfo>> tagMap) {
        return reviews.stream()
                .map(review -> ReviewInfo.builder()
                        .userId(review.getUserId())
                        .content(review.getContent())
                        .score(review.getScore())
                        .imageUrls(imageMap.getOrDefault(review.getId(), List.of()))
                        .tags(tagMap.getOrDefault(review.getId(), List.of()))
                        .build())
                .toList();
    }

    private List<Long> extractReviewIds(Page<ReviewEntity> reviewPage) {
        return reviewPage.getContent().stream()
                .map(ReviewEntity::getId)
                .toList();
    }
}
