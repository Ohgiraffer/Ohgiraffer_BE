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

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_001", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH_002", "접근 권한이 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_003", "유효하지 않은 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_004", "유효하지 않은 refresh token입니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_005", "만료된 refresh token입니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_006", "아이디 또는 비밀번호가 올바르지 않습니다."),
    WITHDRAWN_MEMBER(HttpStatus.FORBIDDEN, "AUTH_007", "부트캠프 과정을 중도 종료하신 계정으로, 서비스 접근 권한이 만료되었습니다."),
    EXPELLED_MEMBER(HttpStatus.FORBIDDEN, "AUTH_008", "현재 이 계정은 부트캠프 운영 정책에 따라 서비스 이용이 제한되었습니다. "),
    MISSING_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_009", "AccessToken이 필요합니다."),
    MISSING_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_010", "RefreshToken이 필요합니다."),
    ALREADY_LOGGED_OUT(HttpStatus.UNAUTHORIZED, "AUTH_011", "이미 로그아웃 되었습니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "사용자를 찾을 수 없습니다."),
    PASSWORD_RESET_NOT_REQUIRED(HttpStatus.FORBIDDEN, "USER_002", "이미 비밀번호를 변경하였습니다."),
    MISSING_PROFILE_IMAGE(HttpStatus.BAD_REQUEST, "USER_003", "업로드 할 프로필 이미지가 필요합니다."),
    PROFILE_IMAGE_TOO_LARGE(HttpStatus.CONTENT_TOO_LARGE, "USER_004", "프로필 이미지 크기는 50MB를 초과할 수 없습니다."),
    INVALID_PROFILE_IMAGE_TYPE(HttpStatus.BAD_REQUEST, "USER_005", "프로필 이미지는 JPG, PNG 형식만 업로드할 수 있습니다."),

    GOOGLE_SHEET_INVALID_URL(HttpStatus.BAD_REQUEST, "SHEET_001", "올바른 Google 스프레드시트 URL이 아닙니다."),
    GOOGLE_SHEET_ACCESS_DENIED(HttpStatus.FORBIDDEN, "SHEET_002", "Google 스프레드시트에 접근할 권한이 없습니다."),
    GOOGLE_SHEET_NOT_FOUND(HttpStatus.NOT_FOUND, "SHEET_003", "Google 스프레드시트를 찾을 수 없습니다."),
    GOOGLE_SHEET_RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "SHEET_004", "Google Sheets API 호출 한도를 초과했습니다. 잠시 후 다시 시도해주세요."),
    GOOGLE_SHEET_API_ERROR(HttpStatus.BAD_GATEWAY, "SHEET_005", "Google Sheets API 호출 중 오류가 발생했습니다."),

    APPROVAL_NOT_FOUND(HttpStatus.NOT_FOUND, "APPROVAL_001", "결재 요청을 찾을 수 없습니다."),
    APPROVAL_INVALID_STATUS(HttpStatus.BAD_REQUEST, "APPROVAL_002", "현재 상태에서는 처리할 수 없는 결재 요청입니다."),
    APPROVAL_ACCESS_DENIED(HttpStatus.FORBIDDEN, "APPROVAL_003", "해당 결재 요청을 처리할 권한이 없습니다."),
    SIGNATURE_NOT_FOUND(HttpStatus.NOT_FOUND, "APPROVAL_004", "전자서명을 찾을 수 없습니다."),
    SIGNATURE_ALREADY_EXISTS(HttpStatus.CONFLICT, "APPROVAL_008", "이미 등록된 전자서명이 있습니다."),
    LEAVE_BALANCE_NOT_ENOUGH(HttpStatus.BAD_REQUEST, "APPROVAL_005", "잔여 휴가가 부족합니다."),
    BUDGET_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "APPROVAL_006", "예산 카테고리를 찾을 수 없습니다."),
    PDF_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "APPROVAL_007", "결재 문서 PDF 생성에 실패했습니다."),

    CHAT_CHANNEL_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_001", "채팅 채널을 찾을 수 없습니다."),
    CHAT_MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_002", "메시지를 찾을 수 없습니다."),
    CHAT_REPLY_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_003", "답글을 찾을 수 없습니다."),
    CHAT_MESSAGE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "CHAT_004", "본인이 작성한 메시지만 수정·삭제할 수 있습니다."),
    CHAT_MESSAGE_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "CHAT_005", "이미 삭제된 메시지입니다."),
    CHAT_SENDBIRD_API_ERROR(HttpStatus.BAD_GATEWAY, "CHAT_006", "Sendbird API 호출 중 오류가 발생했습니다."),
    CHAT_WEBHOOK_SIGNATURE_INVALID(HttpStatus.BAD_REQUEST, "CHAT_007", "웹훅 서명 검증에 실패했습니다."),

    GOOGLE_FORM_ACCESS_DENIED(HttpStatus.FORBIDDEN, "FORM_001", "Google Form에 접근할 권한이 없습니다."),
    GOOGLE_FORM_NOT_FOUND(HttpStatus.NOT_FOUND, "FORM_002", "Google Form을 찾을 수 없습니다."),
    GOOGLE_FORM_RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "FORM_003", "Google Forms API 호출 한도를 초과했습니다. 잠시 후 다시 시도해주세요."),
    GOOGLE_FORM_API_ERROR(HttpStatus.BAD_GATEWAY, "FORM_004", "Google Forms API 호출 중 오류가 발생했습니다."),
    SURVEY_FORM_NOT_FOUND(HttpStatus.NOT_FOUND, "SURVEY_001", "설문 폼을 찾을 수 없습니다."),
    SURVEY_FORM_INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "SURVEY_002", "변경할 수 없는 설문 상태입니다."),
    SURVEY_FORM_HAS_RESPONSES(HttpStatus.CONFLICT, "SURVEY_003", "응답이 존재하는 설문은 삭제할 수 없습니다."),

    SUBMISSION_BOX_NOT_FOUND(HttpStatus.NOT_FOUND, "SUBMISSION_001", "제출함을 찾을 수 없습니다."),
    SUBMISSION_BOX_HAS_SUBMISSIONS(HttpStatus.CONFLICT, "SUBMISSION_002", "제출물이 존재하는 제출함은 삭제할 수 없습니다."),
    SUBMISSION_BOX_ACCESS_DENIED(HttpStatus.FORBIDDEN, "SUBMISSION_003", "해당 제출함을 수정하거나 삭제할 권한이 없습니다."),

    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTICE_001", "존재하지 않는 공지입니다."),
    NOTICE_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTICE_002", "존재하지 않는 공지 카테고리입니다."),
    NOTICE_NOT_AUTHOR(HttpStatus.FORBIDDEN, "NOTICE_003", "공지 작성자만 수정하거나 삭제할 수 있습니다."),
    NOTICE_NOT_MANDATORY(HttpStatus.BAD_REQUEST, "NOTICE_004", "필수 공지만 확인 처리할 수 있습니다."),
    NOTICE_CATEGORY_DUPLICATE_NAME(HttpStatus.CONFLICT, "NOTICE_005", "이미 같은 이름의 카테고리가 있습니다."),
    NOTICE_CATEGORY_IN_USE(HttpStatus.CONFLICT, "NOTICE_006", "공지가 사용 중인 카테고리는 삭제할 수 없습니다.");

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