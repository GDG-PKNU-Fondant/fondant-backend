package com.fondant.review.presentation.dto.info;

import lombok.Builder;

import java.util.List;

public record ReviewInfo(
        Long userId,
        List<String> imageUrls,
        String content,
        Double score,
        List<TagInfo> tags
) {
    @Builder
    public ReviewInfo(Long userId, List<String> imageUrls, String content, Double score, List<TagInfo> tags) {
        this.userId = userId;
        this.imageUrls = imageUrls;
        this.content = content;
        this.score = score;
        this.tags = tags;
    }
}
