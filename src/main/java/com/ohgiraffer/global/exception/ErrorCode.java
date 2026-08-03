package com.ohgiraffer.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_001", "입력값이 올바르지 않습니다."),
    INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST, "COMMON_002", "요청 본문이 올바르지 않습니다."),
    MISSING_REQUEST_PARAMETER(HttpStatus.BAD_REQUEST, "COMMON_003", "필수 요청 파라미터가 누락되었습니다."),
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "COMMON_004", "요청 파라미터 타입이 올바르지 않습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_005", "지원하지 않는 HTTP 메서드입니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_006", "요청한 대상을 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_999", "서버 내부 오류가 발생했습니다."),

    GOOGLE_SHEET_INVALID_URL(HttpStatus.BAD_REQUEST, "SHEET_001", "올바른 Google 스프레드시트 URL이 아닙니다."),
    GOOGLE_SHEET_ACCESS_DENIED(HttpStatus.FORBIDDEN, "SHEET_002", "Google 스프레드시트에 접근할 권한이 없습니다."),
    GOOGLE_SHEET_NOT_FOUND(HttpStatus.NOT_FOUND, "SHEET_003", "Google 스프레드시트를 찾을 수 없습니다."),
    GOOGLE_SHEET_RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "SHEET_004", "Google Sheets API 호출 한도를 초과했습니다. 잠시 후 다시 시도해주세요."),
    GOOGLE_SHEET_API_ERROR(HttpStatus.BAD_GATEWAY, "SHEET_005", "Google Sheets API 호출 중 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(
            HttpStatus status,
            String code,
            String message
    ) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}