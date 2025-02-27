package com.fondant.product.presentation.dto.response;

import com.fondant.product.application.dto.ImageInfo;
import com.fondant.product.application.dto.OptionInfo;
import lombok.Builder;

import java.util.List;

@Builder
public record ProductDetailResponse(
        List<ImageInfo> photos,
        String name,
        List<OptionInfo> options,
        String description,
        List<ImageInfo> detailPages,
        int basePrice
) {
}
