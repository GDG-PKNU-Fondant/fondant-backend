package com.fondant.review.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review_tag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewTagEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private ReviewEntity review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hashtag_id")
    private TagEntity hashtag;

    @Builder
    public ReviewTagEntity(ReviewEntity review, TagEntity hashtag) {
        this.review = review;
        this.hashtag = hashtag;
    }
}


