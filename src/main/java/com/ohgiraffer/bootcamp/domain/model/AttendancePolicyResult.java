package com.ohgiraffer.bootcamp.domain.model;

import java.math.BigDecimal;

public record AttendancePolicyResult(
        BigDecimal cautionThresholdPct,
        BigDecimal warningThresholdPct,
        BigDecimal periodExpulsionPct
) {
}