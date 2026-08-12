package com.ohgiraffer.submission.application.usecase;

public enum StudentSubmissionHistoryStatus {

    SUBMITTED,
    NOT_SUBMITTED,
    RESPONDED,
    NOT_RESPONDED,

    // Google Forms API 장애로 응답 여부를 확인하지 못한 상태
    RESPONSE_CHECK_FAILED
}