package com.fondant.review.domain.repository;

import com.fondant.review.domain.entity.ReviewTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewTagRepository extends JpaRepository<ReviewTagEntity,Long> {
    List<ReviewTagEntity> findByReviewIdIn(List<Long>reviewIds);
}
