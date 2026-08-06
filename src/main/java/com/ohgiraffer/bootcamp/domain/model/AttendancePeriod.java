package com.ohgiraffer.bootcamp.domain.model;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class AttendancePeriod {

    private final Long id;
    private final Integer periodNo;
    private final LocalDate periodStart;
    private final LocalDate periodEnd;
    private final Long bootcampId;

    private AttendancePeriod(Long id, Integer periodNo, LocalDate periodStart,
                             LocalDate periodEnd, Long bootcampId) {
        this.id = id;
        this.periodNo = periodNo;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.bootcampId = bootcampId;
    }

    public static AttendancePeriod create(Integer periodNo, LocalDate periodStart,
                                          LocalDate periodEnd, Long bootcampId) {
        if (periodStart.isAfter(periodEnd)) {
            throw new BusinessException(ErrorCode.INVALID_PERIOD_RANGE);
        }
        return new AttendancePeriod(null, periodNo, periodStart, periodEnd, bootcampId);
    }

    public static AttendancePeriod reconstruct(Long id, Integer periodNo, LocalDate periodStart,
                                               LocalDate periodEnd, Long bootcampId) {
        return new AttendancePeriod(id, periodNo, periodStart, periodEnd, bootcampId);
    }
}
