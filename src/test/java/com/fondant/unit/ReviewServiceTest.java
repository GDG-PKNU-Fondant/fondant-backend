package com.fondant.unit;

import com.fondant.review.application.ReviewService;
import com.fondant.review.domain.entity.ReviewEntity;
import com.fondant.review.domain.repository.ReviewRepository;
import com.fondant.review.presentation.dto.info.ReviewInfo;
import com.fondant.review.presentation.dto.request.ReviewSortType;
import com.fondant.review.presentation.dto.response.ReviewsResponse;
import com.fondant.test.repository.UserTestRepository;
import com.fondant.user.domain.entity.Gender;
import com.fondant.user.domain.entity.SNSType;
import com.fondant.user.domain.entity.UserEntity;
import com.fondant.user.domain.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class ReviewServiceTest {
    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserTestRepository userRepository;

    private Long productId;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();

        UserEntity user = userRepository.save(UserEntity.builder()
                .email("user@example.com")
                .name("user")
                .nickname("reviewer")
                .snsType(SNSType.LOCAL)
                .gender(Gender.FEMALE)
                .role(UserRole.USER)
                .birth(Date.valueOf("2000-01-01"))
                .createAt(LocalDate.now())
                .verifiedPhone(true)
                .build());

        productId = 1L;

        ReviewEntity r1 = ReviewEntity.builder().productId(productId).userId(user.getId()).score(4.0).content("중간").build();
        r1.setCreatedAt(LocalDateTime.now().minusDays(2));

        ReviewEntity r2 = ReviewEntity.builder().productId(productId).userId(user.getId()).score(5.0).content("최고").build();
        r2.setCreatedAt(LocalDateTime.now().minusDays(1));

        ReviewEntity r3 = ReviewEntity.builder().productId(productId).userId(user.getId()).score(2.0).content("별로").build();
        r3.setCreatedAt(LocalDateTime.now());

        reviewRepository.saveAll(List.of(r1, r2, r3));
    }

    private Pageable getSortedPageable(ReviewSortType sortType) {
        return switch (sortType) {
            case LATEST -> PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
            case HIGH_SCORE -> PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "score"));
            case LOW_SCORE -> PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "score"));
        };
    }

    @Test
    void 리뷰가_최신순으로_정렬된다() {
        Pageable sortedPageable = getSortedPageable(ReviewSortType.LATEST);
        ReviewsResponse response = reviewService.getReviews(productId, sortedPageable);

        List<ReviewInfo> reviews = response.reviews();
        assertThat(reviews.get(0).content()).isEqualTo("별로");
        assertThat(reviews.get(1).content()).isEqualTo("최고");
        assertThat(reviews.get(2).content()).isEqualTo("중간");
    }

    @Test
    void 리뷰가_별점높은순으로_정렬된다() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("score")));
        ReviewsResponse response = reviewService.getReviews(productId, pageable);

        List<ReviewInfo> reviews = response.reviews();
        assertThat(reviews).hasSize(3);
        assertThat(reviews.get(0).score()).isEqualTo(5.0);
        assertThat(reviews.get(1).score()).isEqualTo(4.0);
        assertThat(reviews.get(2).score()).isEqualTo(2.0);
    }

    @Test
    void 리뷰가_별점낮은순으로_정렬된다() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("score")));
        ReviewsResponse response = reviewService.getReviews(productId, pageable);

        List<ReviewInfo> reviews = response.reviews();
        assertThat(reviews).hasSize(3);
        assertThat(reviews.get(0).score()).isEqualTo(2.0);
        assertThat(reviews.get(1).score()).isEqualTo(4.0);
        assertThat(reviews.get(2).score()).isEqualTo(5.0);
    }
}
