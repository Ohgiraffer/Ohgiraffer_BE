package com.ohgiraffer.bootcamp.domain.model;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class AttendancePolicy {

    private final Long id;
    private BigDecimal cautionThresholdPct;
    private BigDecimal warningThresholdPct;
    private BigDecimal periodExpulsionPct;
    private final Long bootcampId;

    private AttendancePolicy(Long id, BigDecimal cautionThresholdPct, BigDecimal warningThresholdPct,
                             BigDecimal periodExpulsionPct, Long bootcampId) {
        this.id = id;
        this.cautionThresholdPct = cautionThresholdPct;
        this.warningThresholdPct = warningThresholdPct;
        this.periodExpulsionPct = periodExpulsionPct;
        this.bootcampId = bootcampId;
    }

    public static AttendancePolicy create(BigDecimal cautionThresholdPct, BigDecimal warningThresholdPct,
                                          BigDecimal periodExpulsionPct, Long bootcampId) {
        validateRange(cautionThresholdPct, "주의 기준");
        validateRange(warningThresholdPct, "경고 기준");
        validateRange(periodExpulsionPct, "제적위험 기준");

        if (cautionThresholdPct.compareTo(warningThresholdPct) <= 0) {
            throw new BusinessException(ErrorCode.INVALID_POLICY_THRESHOLD_ORDER);
        }
        if (warningThresholdPct.compareTo(periodExpulsionPct) <= 0) {
            throw new BusinessException(ErrorCode.INVALID_POLICY_THRESHOLD_ORDER);
        }
        return new AttendancePolicy(null, cautionThresholdPct, warningThresholdPct, periodExpulsionPct, bootcampId);
    }

    private static void validateRange(BigDecimal value, String fieldName) {
        if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(new BigDecimal("100")) > 0) {
            throw new BusinessException(ErrorCode.INVALID_POLICY_THRESHOLD_RANGE);
        }
    }

    public static AttendancePolicy reconstruct(Long id, BigDecimal cautionThresholdPct, BigDecimal warningThresholdPct,
                                               BigDecimal periodExpulsionPct, Long bootcampId) {
        return new AttendancePolicy(id, cautionThresholdPct, warningThresholdPct, periodExpulsionPct, bootcampId);
    }
}