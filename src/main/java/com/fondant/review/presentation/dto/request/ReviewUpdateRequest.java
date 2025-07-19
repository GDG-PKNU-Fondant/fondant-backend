package com.fondant.review.presentation.dto.request;

import java.util.List;

public record ReviewUpdateRequest(
    List<Long> tagIds,
    String content,
    Double score
) {
}
