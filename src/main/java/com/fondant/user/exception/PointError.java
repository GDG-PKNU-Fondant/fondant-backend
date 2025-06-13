package com.fondant.user.exception;

import com.fondant.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum PointError implements ErrorCode {
    INSUFFICIENT_POINTS(HttpStatus.BAD_REQUEST, "포인트가 충분하지 않습니다.", "INSUFFICIENT_POINTS"),
    INVALID_POINT_AMOUNT(HttpStatus.BAD_REQUEST, "포인트가 충분하지 않습니다.", "INVALID_POINT_AMOUNT");

    private final HttpStatus httpStatus;
    private final String message;
    private final String errorCode;

    PointError(final HttpStatus httpStatus, final String message, final String errorCode) {
        this.httpStatus = httpStatus;
        this.message = message;
        this.errorCode = errorCode;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public String getErrorCode() {
        return this.errorCode;
    }
}