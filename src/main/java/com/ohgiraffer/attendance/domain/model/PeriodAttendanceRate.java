package com.ohgiraffer.attendance.domain.model;

import java.math.BigDecimal;

public record PeriodAttendanceRate(
        Integer periodNo,
        BigDecimal attendanceRate
) {
}
