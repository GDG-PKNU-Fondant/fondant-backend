package com.fondant.market.exception;

import com.fondant.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum MarketError implements ErrorCode {
    MARKET_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 마켓을 찾을 수 없습니다.", "MARKET_NOT_FOUND"),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 카테고리를 찾을 수 없습니다.", "CATEGORY_NOT_FOUND"),
    NO_MARKETS_FOUND(HttpStatus.NO_CONTENT, "등록된 마켓이 없습니다.", "NO_MARKETS_FOUND"),
    INVALID_MARKET_ID(HttpStatus.BAD_REQUEST, "유효하지 않은 marketId입니다.", "INVALID_MARKET_ID"),
    INVALID_CATEGORY_ID(HttpStatus.BAD_REQUEST, "유효하지 않은 categoryId입니다.", "INVALID_CATEGORY_ID"),
    INVALID_PAGE_NUMBER(HttpStatus.BAD_REQUEST, "페이지 번호는 0 이상이어야 합니다.", "INVALID_PAGE_NUMBER");

    private final HttpStatus httpStatus;
    private final String message;
    private final String errorCode;

    MarketError(final HttpStatus httpStatus, final String message, final String errorCode) {
        this.httpStatus = httpStatus;
        this.message = message;
        this.errorCode = errorCode;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}
