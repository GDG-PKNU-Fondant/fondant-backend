package com.fondant.review.domain.repository;

import com.fondant.review.domain.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<ReviewEntity,Long> {
}
