package com.ohgiraffer.attendance.domain.model;

public enum AttendanceStatus {
    PRESENT, // 출석
    LATE, // 지각
    EARLY_LEAVE, // 조퇴
    OUTING, // 외출
    ABSENT, // 결석
    LEAVE, // 휴가
    SICK // 병결
}
