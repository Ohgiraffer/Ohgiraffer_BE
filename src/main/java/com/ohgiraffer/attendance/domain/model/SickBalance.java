package com.ohgiraffer.attendance.domain.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class SickBalance {

    private final Long id;
    private final Long userId;
    private final LocalDate periodStart;
    private final LocalDate periodEnd;
    private final BigDecimal totalDays;
    private final BigDecimal usedDays;
    private final BigDecimal carriedOverDays;

    private SickBalance(Long id, Long userId, LocalDate periodStart, LocalDate periodEnd,
                        BigDecimal totalDays, BigDecimal usedDays, BigDecimal carriedOverDays) {
        this.id = id;
        this.userId = userId;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.totalDays = totalDays;
        this.usedDays = usedDays;
        this.carriedOverDays = carriedOverDays;
    }

    public static SickBalance reconstitute(Long id, Long userId, LocalDate periodStart, LocalDate periodEnd,
                                           BigDecimal totalDays, BigDecimal usedDays, BigDecimal carriedOverDays) {
        return new SickBalance(id, userId, periodStart, periodEnd, totalDays, usedDays, carriedOverDays);
    }

    public int remainingDays() {
        return totalDays.add(carriedOverDays).subtract(usedDays).intValue();
    }

    public static SickBalance create(Long userId, LocalDate periodStart, LocalDate periodEnd,
                                     BigDecimal totalDays, BigDecimal carriedOverDays) {
        return new SickBalance(null, userId, periodStart, periodEnd, totalDays, BigDecimal.ZERO, carriedOverDays);
    }
}
