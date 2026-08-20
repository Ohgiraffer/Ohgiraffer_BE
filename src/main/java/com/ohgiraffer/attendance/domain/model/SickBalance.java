package com.ohgiraffer.attendance.domain.model;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class SickBalance {

    private final Long id;
    private final Long userId;
    private final BigDecimal totalDays;
    private final BigDecimal usedDays;

    private SickBalance(Long id, Long userId, BigDecimal totalDays, BigDecimal usedDays) {
        this.id = id;
        this.userId = userId;
        this.totalDays = totalDays;
        this.usedDays = usedDays;
    }

    public static SickBalance reconstitute(Long id, Long userId, BigDecimal totalDays, BigDecimal usedDays) {
        return new SickBalance(id, userId, totalDays, usedDays);
    }

    public static SickBalance create(Long userId, BigDecimal totalDays) {
        return new SickBalance(null, userId, totalDays, BigDecimal.ZERO);
    }

    public int remainingDays() {
        return totalDays.subtract(usedDays).intValue();
    }
}