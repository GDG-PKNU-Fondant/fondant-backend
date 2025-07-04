package com.fondant.review.domain.repository;


import com.fondant.review.domain.entity.ReviewPhotoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewPhotoRepository extends JpaRepository<ReviewPhotoEntity,Long> {
    List<ReviewPhotoEntity> findByReviewIdIn(List<Long> reviewIds);
}
