package com.fondant.review.presentation.dto.info;

import com.fondant.review.domain.entity.TagCategory;

public record TagInfo(
        TagCategory tagCategory,
        String hashtag
) {
}
