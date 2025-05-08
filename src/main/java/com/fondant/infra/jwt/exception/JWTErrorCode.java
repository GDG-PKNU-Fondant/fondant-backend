package com.fondant.infra.jwt.exception;

public enum JWTErrorCode {
    INVALID_TYPE("T1", "잘못된 타입의 토큰입니다."),
    EXPIRED("T2", "토큰이 만료되었습니다."),
    INVALID("T3", "유효하지 않은 토큰입니다.");

    private final String code;
    private final String message;

    JWTErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static JWTErrorCode fromCode(String code) {
        for (JWTErrorCode value : values()) {
            if (value.code.equals(code)) return value;
        }
        return INVALID;
    }
}