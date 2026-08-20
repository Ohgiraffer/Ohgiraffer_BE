package com.ohgiraffer.attendance.presentation.api.response;

import java.math.BigDecimal;

public record AttendanceDashboardSummaryResponse(
        BigDecimal averageAttendanceRate,
        BigDecimal expectedCompletionRate,
        int totalStudents,
        int activeStudents,
        int attendedTodayCount, // 오늘 출근 인원 (구글 시트 동기화 기준, 당일 데이터)
        int managedStudents,    // 정상 (기간 누적 위험도 기준)
        int cautionStudents,    // 주의
        int warningStudents,    // 경고
        int riskStudents,       // 제적위험
        int atRiskStudents,     // 주의+경고+제적위험 합
        int dropoutStudents
) {
}