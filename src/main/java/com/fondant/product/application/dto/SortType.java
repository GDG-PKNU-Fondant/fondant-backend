package com.fondant.product.application.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SortType {
    DISCOUNT("할인순"),
    REVIEW("리뷰 많은 순"),
    SALES("판매량 순"),
    PRICE_ASC("낮은 가격순"),
    PRICE_DESC("높은 가격순");

    private final String description;
}
