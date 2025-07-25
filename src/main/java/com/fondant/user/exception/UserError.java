package com.fondant.user.exception;

import com.fondant.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum UserError implements ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 유저를 찾을 수 없습니다.", "USER_NOT_FOUND"),
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 주소를 찾을 수 없습니다.", "ADDRESS_NOT_FOUND"),
    VERIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 요청이 존재하지 않습니다.", "VERIFICATION_NOT_FOUND"),
    VERIFICATION_IS_EXPIRED(HttpStatus.GONE, "인증번호가 만료되었습니다.", "VERIFICATION_IS_EXPIRED"),
    VERIFICATION_NOT_MATCH(HttpStatus.BAD_REQUEST, "인증번호가 일치하지 않습니다.", "VERIFICATION_NOT_MATCH"),
    VERIFICATION_NOT_DELETED(HttpStatus.BAD_REQUEST, "인증번호를 삭제하지 못했습니다.", "VERIFICATION_NOT_DELETED"),
    SMS_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,"SMS 전송에 실패했습니다.", "SMS_SEND_FAILED"),
    REFRESH_EXPIRED(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 만료되었습니다.", "REFRESH_EXPIRED"),
    REFRESH_INVALID(HttpStatus.BAD_REQUEST,  "리프레시 토큰이 유효하지 않습니다.", "REFRESH_INVALID"),
    INVALID_POINT(HttpStatus.BAD_REQUEST,"포인트가 부족합니다." , "INVALID_POINT");

    private final HttpStatus httpStatus;
    private final String message;
    private final String errorCode;

    UserError(final HttpStatus httpStatus, final String message, final String errorCode) {
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