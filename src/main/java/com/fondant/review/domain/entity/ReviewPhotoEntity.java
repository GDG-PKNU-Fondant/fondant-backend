package com.fondant.review.domain.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review_photo")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewPhotoEntity {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private ReviewEntity review;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Builder
    public ReviewPhotoEntity(ReviewEntity review, String imageUrl) {
        this.review = review;
        this.imageUrl = imageUrl;
    }
}


