package com.fondant.review.presentation.dto.request;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record ReviewCreateRequest(
    List<Long> tagIds,
    String content,
    Double score
) {
}