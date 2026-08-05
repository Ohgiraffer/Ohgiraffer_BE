package com.ohgiraffer.bootcamp.application.policy;

import com.ohgiraffer.bootcamp.domain.model.AttendancePeriod;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;

import java.util.Comparator;
import java.util.List;

public class AttendancePeriodPolicy {

    private AttendancePeriodPolicy() {}

    public static void validate(List<AttendancePeriod> periods) {
        validateNoDuplicatePeriodNo(periods);
        validateNoOverlap(periods);
    }

    private static void validateNoDuplicatePeriodNo(List<AttendancePeriod> periods) {
        List<Integer> periodNos = periods.stream()
                .map(AttendancePeriod::getPeriodNo)
                .toList();
        if (periodNos.size() != periodNos.stream().distinct().count()) {
            throw new BusinessException(ErrorCode.DUPLICATE_PERIOD_NO);
        }
    }

    private static void validateNoOverlap(List<AttendancePeriod> periods) {
        List<AttendancePeriod> sorted = periods.stream()
                .sorted(Comparator.comparing(AttendancePeriod::getPeriodStart))
                .toList();
        for (int i = 0; i < sorted.size() - 1; i++) {
            if (!sorted.get(i).getPeriodEnd().isBefore(sorted.get(i + 1).getPeriodStart())) {
                throw new BusinessException(ErrorCode.OVERLAPPING_PERIOD);
            }
        }
    }
}
