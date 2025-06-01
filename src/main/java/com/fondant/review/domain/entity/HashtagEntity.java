package com.fondant.review.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "hashtag")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HashtagEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TagCategory category;

    @Column(nullable = false)
    private String content;

    @Builder
    public HashtagEntity(TagCategory category, String content) {
        this.category = category;
        this.content = content;
    }

    public enum TagCategory {
        SERVICE, TASTE, PRICE
    }
}
