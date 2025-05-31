package com.fondant.order.exception;

import com.fondant.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum OrderError implements ErrorCode {
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 주문을 찾을 수 없습니다.", "ORDER_NOT_FOUND"),
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 주소를 찾을 수 없습니다.", "ADDRESS_NOT_FOUND"),
    INVALID_ORDER(HttpStatus.UNPROCESSABLE_ENTITY, "잘못된 주문 요청입니다.", "INVALID_ORDER");

    private final HttpStatus httpStatus;
    private final String message;
    private final String errorCode;

    OrderError(final HttpStatus httpStatus, final String message, final String errorCode) {
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
