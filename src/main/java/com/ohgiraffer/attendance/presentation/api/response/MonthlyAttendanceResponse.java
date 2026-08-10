package com.ohgiraffer.attendance.presentation.api.response;

import com.ohgiraffer.attendance.domain.model.CalendarStatusGroup;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record MonthlyAttendanceResponse(
        String yearMonth,
        List<DayInfo> days
) {
    public record DayInfo(
            LocalDate date,
            CalendarStatusGroup status,
            LocalTime checkInTime,
            LocalTime checkOutTime
    ) {}
}