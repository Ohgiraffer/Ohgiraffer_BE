package com.ohgiraffer.attendance.domain.model;

public enum SyncResult {
    SUCCESS, // 실패 행 없음 (처리된 행이 0개인 경우도 포함)
    PARTIAL, // 성공/실패 행이 함께 존재
    FAIL     // 시도한 행이 모두 실패, 또는 동기화 자체가 수행되지 않음
}