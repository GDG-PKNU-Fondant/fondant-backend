package com.fondant.payment.exception;

import com.fondant.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum PaymentError implements ErrorCode {
    GET_PAYMENT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "결제 정보 조회에 실패했습니다.", "GET_PAYMENT_FAILED"),
    PAYMENT_NOT_SUCCESS(HttpStatus.BAD_REQUEST, "결제가 정상적으로 완료되지 않았습니다.", "PAYMENT_NOT_SUCCESS"),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "결제 금액이 일치하지 않습니다.", "PAYMENT_AMOUNT_MISMATCH"),
    REFUND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "환불 처리에 실패했습니다.", "REFUND_FAILED"),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "결제 정보를 찾을 수 없습니다.", "PAYMENT_NOT_FOUND");

    private final HttpStatus httpStatus;
    private final String message;
    private final String errorCode;

    PaymentError(final HttpStatus httpStatus, final String message, final String errorCode) {
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
