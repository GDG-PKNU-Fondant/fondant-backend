package com.fondant.review.domain.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review_photo")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewPhotoEntity {
    @Id
    @GeneratedValue
    private Long id;

    @NotNull
    @Column(name="review_id",nullable = false)
    @Getter
    private Long reviewId;

    @Column(name = "image_url", nullable = false)
    @Getter
    private String imageUrl;

    @Builder
    public ReviewPhotoEntity(Long reviewId, String imageUrl) {
        this.reviewId = reviewId;
        this.imageUrl = imageUrl;
    }
}


