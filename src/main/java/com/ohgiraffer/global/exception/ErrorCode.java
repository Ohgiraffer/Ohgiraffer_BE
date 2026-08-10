package com.ohgiraffer.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_001", "입력값이 올바르지 않습니다."),
    INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST, "COMMON_002", "요청 본문이 올바르지 않습니다."),
    MISSING_REQUEST_PARAMETER(HttpStatus.BAD_REQUEST, "COMMON_003", "필수 요청 파라미터가 누락되었습니다."),
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "COMMON_004", "요청 파라미터 타입이 올바르지 않습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_005", "지원하지 않는 HTTP 메서드입니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_006", "요청한 대상을 찾을 수 없습니다."),
    FILE_STORAGE_UPLOAD_FAILED(HttpStatus.BAD_GATEWAY, "COMMON_007", "파일 업로드에 실패했습니다."),
    FILE_STORAGE_DELETE_FAILED(HttpStatus.BAD_GATEWAY, "COMMON_008", "파일 삭제에 실패했습니다."),
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
    INVALID_USER_STATUS_TARGET(HttpStatus.BAD_REQUEST, "USER_006", "제적 또는 자퇴 상태로만 변경할 수 있습니다."),
    USER_ALREADY_INACTIVE(HttpStatus.CONFLICT, "USER_007", "이미 자퇴/제적 처리된 훈련생입니다."),
    USER_ALREADY_COMPLETED(HttpStatus.CONFLICT, "USER_008", "이미 수료 완료된 훈련생은 자퇴/제적 처리할 수 없습니다."),
    USER_BULK_INSERT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "USER_009", "등록 중 오류가 발생했습니다. 다시 시도해주세요."),

    GOOGLE_SHEET_INVALID_URL(HttpStatus.BAD_REQUEST, "SHEET_001", "올바른 Google 스프레드시트 URL이 아닙니다."),
    GOOGLE_SHEET_ACCESS_DENIED(HttpStatus.FORBIDDEN, "SHEET_002", "Google 스프레드시트에 접근할 권한이 없습니다."),
    GOOGLE_SHEET_NOT_FOUND(HttpStatus.NOT_FOUND, "SHEET_003", "Google 스프레드시트를 찾을 수 없습니다."),
    GOOGLE_SHEET_RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "SHEET_004", "Google Sheets API 호출 한도를 초과했습니다. 잠시 후 다시 시도해주세요."),
    GOOGLE_SHEET_API_ERROR(HttpStatus.BAD_GATEWAY, "SHEET_005", "Google Sheets API 호출 중 오류가 발생했습니다."),

    FILE_PARSE_FAILED(HttpStatus.BAD_REQUEST, "FILE_001", "파일을 읽는 중 오류가 발생했습니다."),
    UNSUPPORTED_FILE_TYPE(HttpStatus.BAD_REQUEST, "FILE_002", "지원하지 않는 파일 형식입니다. CSV 또는 엑셀 파일만 업로드 가능합니다."),

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

    NOTI_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTI_001", "알림을 찾을 수 없습니다."),
    NOTI_ACCESS_DENIED(HttpStatus.FORBIDDEN, "NOTI_002", "본인의 알림만 접근할 수 있습니다."),
    NOTI_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "NOTI_003", "이미 삭제된 알림입니다."),
    NOTI_IDS_REQUIRED(HttpStatus.BAD_REQUEST, "NOTI_004", "삭제할 알림을 선택해주세요."),
    NOTI_SSE_CONNECTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "NOTI_005", "실시간 알림 연결에 실패했습니다."),

    GOOGLE_FORM_ACCESS_DENIED(HttpStatus.FORBIDDEN, "FORM_001", "Google Form에 접근할 권한이 없습니다."),
    GOOGLE_FORM_NOT_FOUND(HttpStatus.NOT_FOUND, "FORM_002", "Google Form을 찾을 수 없습니다."),
    GOOGLE_FORM_RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "FORM_003", "Google Forms API 호출 한도를 초과했습니다. 잠시 후 다시 시도해주세요."),
    GOOGLE_FORM_API_ERROR(HttpStatus.BAD_GATEWAY, "FORM_004", "Google Forms API 호출 중 오류가 발생했습니다."),
    SURVEY_FORM_NOT_FOUND(HttpStatus.NOT_FOUND, "SURVEY_001", "설문 폼을 찾을 수 없습니다."),
    SURVEY_FORM_INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "SURVEY_002", "변경할 수 없는 설문 상태입니다."),
    SURVEY_FORM_HAS_RESPONSES(HttpStatus.CONFLICT, "SURVEY_003", "응답이 존재하는 설문은 삭제할 수 없습니다."),
    SURVEY_FORM_ACCESS_DENIED(HttpStatus.FORBIDDEN, "SURVEY_005", "해당 설문에 접근할 권한이 없습니다."),
    SURVEY_SHEET_LINK_NOT_FOUND(HttpStatus.NOT_FOUND, "SURVEY_006", "설문에 연결된 Google Sheet가 없습니다."),
    SURVEY_RESPONSE_NOT_FOUND(HttpStatus.BAD_REQUEST, "SURVEY_007", "요약할 설문 응답이 없습니다."),
    SURVEY_PDF_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SURVEY_008", "설문 결과 PDF 생성에 실패했습니다."),

    SUBMISSION_BOX_NOT_FOUND(HttpStatus.NOT_FOUND, "SUBMISSION_001", "제출함을 찾을 수 없습니다."),
    SUBMISSION_BOX_HAS_SUBMISSIONS(HttpStatus.CONFLICT, "SUBMISSION_002", "제출물이 존재하는 제출함은 삭제할 수 없습니다."),
    SUBMISSION_BOX_ACCESS_DENIED(HttpStatus.FORBIDDEN, "SUBMISSION_003", "해당 제출함을 수정하거나 삭제할 권한이 없습니다."),
    SUBMISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "SUBMISSION_004", "제출물을 찾을 수 없습니다."),
    SUBMISSION_ALREADY_EXISTS(HttpStatus.CONFLICT, "SUBMISSION_005", "이미 제출한 제출함입니다."),
    SUBMISSION_NOT_STARTED(HttpStatus.BAD_REQUEST, "SUBMISSION_006", "아직 제출이 시작되지 않았습니다."),
    SUBMISSION_DEADLINE_EXPIRED(HttpStatus.BAD_REQUEST, "SUBMISSION_007", "제출 마감 시간이 지났습니다."),
    SUBMISSION_ITEM_MISMATCH(HttpStatus.BAD_REQUEST, "SUBMISSION_008", "제출 항목이 제출함 설정과 일치하지 않습니다."),
    SUBMISSION_FILE_TYPE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "SUBMISSION_009", "허용되지 않은 파일 형식입니다."),
    SUBMISSION_TEAM_NOT_FOUND(HttpStatus.BAD_REQUEST, "SUBMISSION_010", "현재 소속된 팀을 찾을 수 없습니다."),
    SUBMISSION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "SUBMISSION_011", "해당 제출함에 제출할 권한이 없습니다."),
    SUBMISSION_FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "SUBMISSION_012", "제출 파일의 크기가 허용 범위를 초과했습니다."),
    SUBMISSION_FILE_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "SUBMISSION_013", "제출할 수 있는 파일 개수를 초과했습니다."),
    SUBMISSION_FILE_NAME_TOO_LONG(HttpStatus.BAD_REQUEST, "SUBMISSION_014", "제출 파일명이 허용 길이를 초과했습니다."),
    SUBMISSION_FILE_UPLOAD_FAILED(HttpStatus.BAD_GATEWAY, "SUBMISSION_015", "파일 저장소에 파일을 업로드하지 못했습니다."),
    SUBMISSION_TEAM_DATA_INCONSISTENT(HttpStatus.INTERNAL_SERVER_ERROR, "SUBMISSION_016", "학생의 팀 소속 정보가 올바르지 않습니다."),
    SUBMISSION_FILE_DOWNLOAD_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "SUBMISSION_017", "파일 제출 항목만 다운로드할 수 있습니다."),
    SUBMISSION_FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "SUBMISSION_018", "제출 파일을 찾을 수 없습니다."),
    SUBMISSION_FILE_PREVIEW_NOT_SUPPORTED(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "SUBMISSION_019", "미리보기를 지원하지 않는 파일 형식입니다."),
    SUBMISSION_BOX_STRUCTURE_CHANGE_NOT_ALLOWED(HttpStatus.CONFLICT, "SUBMISSION_020", "제출물이 존재하는 제출함의 제출 단위 또는 제출 항목 구조는 변경할 수 없습니다."),

    SPACE_NOT_FOUND(HttpStatus.NOT_FOUND, "SPACE_001", "공간을 찾을 수 없습니다."),
    SPACE_NAME_DUPLICATED(HttpStatus.CONFLICT, "SPACE_002", "이미 사용 중인 공간명입니다."),
    SPACE_HAS_OCCUPANTS(HttpStatus.CONFLICT, "SPACE_003", "현재 이용자가 있는 공간은 삭제할 수 없습니다."),
    SPACE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "SPACE_004", "공간을 관리할 권한이 없습니다."),
    SPACE_CAPACITY_EXCEEDED(HttpStatus.CONFLICT, "SPACE_005", "해당 공간의 최대 수용 인원을 초과했습니다."),

    TEAM_DUPLICATE_NAME(HttpStatus.CONFLICT, "TEAM_003", "이미 존재하는 팀명입니다."),
    TEAM_INVALID_PERIOD(HttpStatus.BAD_REQUEST, "TEAM_004", "팀 시작일은 종료일보다 늦을 수 없습니다."),
    TEAM_ALREADY_DISSOLVED(HttpStatus.CONFLICT, "TEAM_005", "이미 해체된 팀입니다."),
    TEAM_MEMBER_ALREADY_ASSIGNED(HttpStatus.CONFLICT, "TEAM_006", "이미 팀에 배정된 훈련생입니다."),
    TEAM_MEMBER_INVALID_USER(HttpStatus.BAD_REQUEST, "TEAM_008", "팀에는 활성 훈련생만 배정할 수 있습니다."),
    TEAM_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "TEAM_007", "팀원 배정 정보를 찾을 수 없습니다."),
    TEAM_MEMBER_ALREADY_LEFT(HttpStatus.CONFLICT, "TEAM_009", "이미 종료된 팀원 배정입니다."),
    TEAM_MEMBER_TEAM_MISMATCH(HttpStatus.BAD_REQUEST, "TEAM_010", "팀원 배정 정보가 요청한 팀과 일치하지 않습니다."),
    TEAM_SAME_TARGET(HttpStatus.BAD_REQUEST, "TEAM_011", "같은 팀으로는 이동할 수 없습니다."),
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "TEAM_001", "팀을 찾을 수 없습니다."),
    TEAM_ACCESS_DENIED(HttpStatus.FORBIDDEN, "TEAM_002", "팀 관리에 접근할 권한이 없습니다."),

    ATTENDANCE_PERIOD_NOT_FOUND(HttpStatus.NOT_FOUND, "ATTENDANCE_001", "존재하지 않거나 소속 부트캠프의 단위기간이 아닙니다."),
    LEAVE_BALANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "ATTENDANCE_002", "해당 기간의 휴가 잔여일수 정보를 찾을 수 없습니다."),
    SICK_BALANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "ATTENDANCE_003", "해당 기간의 병결 잔여일수 정보를 찾을 수 없습니다."),
    ATTENDANCE_APPROVAL_CONFLICT(HttpStatus.CONFLICT, "ATTENDANCE_004", "다른 승인 건으로 이미 처리된 날짜입니다."),

    BOOTCAMP_NOT_FOUND(HttpStatus.NOT_FOUND, "BOOTCAMP_001", "부트캠프를 찾을 수 없습니다."),
    INVALID_PERIOD_RANGE(HttpStatus.BAD_REQUEST, "BOOTCAMP_002", "단위기간 시작일이 종료일보다 늦을 수 없습니다."),
    DUPLICATE_PERIOD_NO(HttpStatus.BAD_REQUEST, "BOOTCAMP_003", "단위기간 번호가 중복되었습니다."),
    OVERLAPPING_PERIOD(HttpStatus.BAD_REQUEST, "BOOTCAMP_004", "단위기간이 서로 겹칩니다."),
    INVALID_POLICY_THRESHOLD_ORDER(HttpStatus.BAD_REQUEST, "BOOTCAMP_005", "출석률 기준은 주의 > 경고 > 제적위험 순이어야 합니다."),
    INVALID_POLICY_THRESHOLD_RANGE(HttpStatus.BAD_REQUEST, "BOOTCAMP_006", "출석률 기준은 0 이상 100 이하여야 합니다."),
    BOOTCAMP_ALREADY_REGISTERED(HttpStatus.CONFLICT, "BOOTCAMP_007", "이미 등록된 부트캠프가 있습니다."),
    BOOTCAMP_ACCESS_DENIED(HttpStatus.FORBIDDEN, "BOOTCAMP_008", "소속 부트캠프가 아닌 학생의 정보에는 접근할 수 없습니다."),

    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTICE_001", "존재하지 않는 공지입니다."),
    NOTICE_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTICE_002", "존재하지 않는 공지 카테고리입니다."),
    NOTICE_NOT_AUTHOR(HttpStatus.FORBIDDEN, "NOTICE_003", "공지 작성자만 수정하거나 삭제할 수 있습니다."),
    NOTICE_CATEGORY_DUPLICATE_NAME(HttpStatus.CONFLICT, "NOTICE_005", "이미 같은 이름의 카테고리가 있습니다."),
    NOTICE_CATEGORY_IN_USE(HttpStatus.CONFLICT, "NOTICE_006", "공지가 사용 중인 카테고리는 삭제할 수 없습니다."),
    NOTICE_ATTACHMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTICE_007", "존재하지 않는 첨부파일입니다."),
    NOTICE_ATTACHMENT_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "NOTICE_008", "공지 하나에 첨부할 수 있는 파일 개수를 넘었습니다."),
    NOTICE_ATTACHMENT_TOO_LARGE(HttpStatus.BAD_REQUEST, "NOTICE_009", "첨부파일 크기가 허용 범위를 넘었습니다."),
    NOTICE_IMAGE_TYPE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "NOTICE_010", "본문에 넣을 수 없는 이미지 형식입니다."),
    NOTICE_ATTACHMENT_TYPE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "NOTICE_011", "첨부할 수 없는 파일 형식입니다."),

    CONSULTATION_NOT_FOUND(HttpStatus.NOT_FOUND, "CONSULTATION_001", "존재하지 않는 상담입니다."),
    CONSULTATION_ALREADY_BOOKED(HttpStatus.CONFLICT, "CONSULTATION_002", "이미 예약된 시간입니다."),
    CONSULTATION_ALREADY_CLOSED(HttpStatus.CONFLICT, "CONSULTATION_003", "이미 종료되었거나 취소된 상담입니다."),
    CONSULTATION_TIME_IN_USE(HttpStatus.CONFLICT, "CONSULTATION_004", "이미 예약이 잡힌 시간은 가능 시간에서 해제할 수 없습니다."),

    CALENDAR_EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CALENDAR_001", "존재하지 않는 일정입니다."),
    CALENDAR_EVENT_NOT_CREATOR(HttpStatus.FORBIDDEN, "CALENDAR_002", "일정을 등록한 사람만 삭제할 수 있습니다."),
    AI_API_CALL_FAILED(HttpStatus.BAD_GATEWAY, "AI_001", "AI API 호출 중 오류가 발생했습니다.");

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
