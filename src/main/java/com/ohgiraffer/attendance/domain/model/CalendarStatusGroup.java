package com.ohgiraffer.attendance.domain.model;

public enum CalendarStatusGroup {

    NORMAL,     // 출석·휴가·병결
    IRREGULAR,  // 지각·조퇴·외출
    ABSENT;     // 결석

    public static CalendarStatusGroup from(AttendanceStatus status) {
        return switch (status) {
            case PRESENT, LEAVE, SICK -> NORMAL;
            case LATE, EARLY_LEAVE, OUTING -> IRREGULAR;
            case ABSENT -> ABSENT;
        };
    }
}