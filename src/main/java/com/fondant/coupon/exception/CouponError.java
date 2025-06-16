package com.fondant.coupon.exception;

import com.fondant.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum CouponError implements ErrorCode {
    COUPON_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 쿠폰입니다.", "COUPON_NOT_FOUND"),
    COUPON_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "사용할 수 없는 쿠폰입니다.", "COUPON_NOT_AVAILABLE"),
    ALREADY_USED_COUPON(HttpStatus.BAD_REQUEST, "이미 사용한 쿠폰입니다.", "ALREADY_USED_COUPON")
    ;

    private final HttpStatus httpStatus;
    private final String message;
    private final String errorCode;

    CouponError(final HttpStatus httpStatus, final String message, final String errorCode) {
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
