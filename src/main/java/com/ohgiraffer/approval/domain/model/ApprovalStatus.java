package com.ohgiraffer.approval.domain.model;

public enum ApprovalStatus {
    PENDING,    // 신청됨
    CHECKED,    // 확인됨
    APPROVED,   // 승인됨
    REJECTED,   // 반려됨
    COMPLETED   // 후속 처리 완료
}