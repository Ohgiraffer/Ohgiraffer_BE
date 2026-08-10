package com.ohgiraffer.attendance.presentation.api.response;

import java.math.BigDecimal;

public record AttendanceDashboardSummaryResponse(
        BigDecimal averageAttendanceRate,
        BigDecimal expectedCompletionRate,
        int totalStudents,
        int activeStudents,
        int managedStudents,
        int atRiskStudents,
        int dropoutStudents
) {
}