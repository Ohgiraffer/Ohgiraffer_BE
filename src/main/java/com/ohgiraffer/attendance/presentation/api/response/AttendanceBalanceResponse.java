package com.ohgiraffer.attendance.presentation.api.response;

public record AttendanceBalanceResponse(
        Integer remainingLeaveDays,
        Integer remainingSickDays
) {
    public static AttendanceBalanceResponse of(Integer remainingLeaveDays, Integer remainingSickDays) {
        return new AttendanceBalanceResponse(remainingLeaveDays, remainingSickDays);
    }
}