package com.ohgiraffer.attendance.domain.dto;

import java.math.BigDecimal;

public record PeriodAttendanceRate(
        Integer periodNo,
        BigDecimal attendanceRate
) {
}
