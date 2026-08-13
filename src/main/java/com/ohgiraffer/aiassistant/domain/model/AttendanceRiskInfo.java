package com.ohgiraffer.aiassistant.domain.model;

import java.time.LocalDateTime;

/*
* comment.
*  출결 위험도 정보 - AI비서 브리핑 전용 값 객체
* */

public record AttendanceRiskInfo(
        Long userId,
        String riskLevel,
        LocalDateTime checkedAt
) {
}
