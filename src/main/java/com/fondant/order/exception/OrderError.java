package com.fondant.order.exception;

import com.fondant.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum OrderError implements ErrorCode {
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 주문을 찾을 수 없습니다.", "ORDER_NOT_FOUND"),
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 주소를 찾을 수 없습니다.", "ADDRESS_NOT_FOUND"),
    INVALID_ORDER(HttpStatus.UNPROCESSABLE_ENTITY, "잘못된 주문 요청입니다.", "INVALID_ORDER"),
    INVALID_COUPON(HttpStatus.BAD_REQUEST, "유효하지 않은 쿠폰입니다.", "INVALID_COUPON"),
    DUPLICATE_COUPON(HttpStatus.BAD_REQUEST, "쿠폰이 중복으로 사용 되었습니다.", "DUPLICATE_COUPON"),
    DUPLICATE_COUPON_ITEM(HttpStatus.BAD_REQUEST, "한 상품에 두 개의 쿠폰을 적용할 수 없습니다.", "DUPLICATE_COUPON_ITEM"),
    COUPON_ITEM_NOT_MATCH(HttpStatus.BAD_REQUEST, "상품에 적용할 수 없는 쿠폰이 있습니다.", "COUPON_ITEM_NOT_MATCH"),
    INVALID_COUPON_SCOPE(HttpStatus.BAD_REQUEST, "쿠폰 적용 범위가 올바르지 않습니다.", "INVALID_COUPON_SCOPE"),
    MIN_PRICE_NOT_MET(HttpStatus.BAD_REQUEST, "쿠폰 최소 주문 금액 미달입니다.", "MIN_PRICE_NOT_MET"),
    OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "매진된 상품입니다.","OUT_OF_STOCK" ),
    CHECKOUT_ITEM_NOT_FOUND(HttpStatus.BAD_REQUEST, "상품이 존재하지 않습니다." , "CHECKOUT_ITEM_NOT_FOUND"),
    INVALID_AMOUNT(HttpStatus.BAD_REQUEST,"주문 금액이 일치하지 않습니다.", "INVALID_AMOUNT");

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
