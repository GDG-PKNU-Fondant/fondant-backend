package com.fondant.review.presentation.dto.response;

import com.fondant.global.dto.PageInfo;
import com.fondant.review.presentation.dto.info.ReviewInfo;
import java.util.List;

public record ReviewsResponse(
    PageInfo pageInfo,
    Long productId,
    int totalCount,
    List<ReviewInfo> reviews
) {
}