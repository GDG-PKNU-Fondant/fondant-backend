package com.fondant.review.domain.repository;

import com.fondant.review.domain.entity.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<TagEntity, Long> {
}
