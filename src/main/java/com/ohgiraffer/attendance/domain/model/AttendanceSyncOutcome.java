package com.ohgiraffer.attendance.domain.model;

public enum AttendanceSyncOutcome {
    SKIPPED,   // 보호 대상(휴가/병결) 또는 입실 기록 없음 등으로 처리하지 않음
    UNCHANGED, // 처리했지만 기존 값과 동일해서 변동 없음
    CHANGED    // 신규 생성 또는 값 변경
}