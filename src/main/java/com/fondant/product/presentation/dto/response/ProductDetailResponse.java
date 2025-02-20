package com.fondant.product.presentation.dto.response;

import com.fondant.product.application.dto.OptionInfo;
import lombok.Builder;

import java.util.List;

@Builder
public record ProductDetailResponse(
        List<String> photos,
        String name,
        List<OptionInfo> options,
        String description,
        List<String> detailPages
) {
}
