package com.ohgiraffer.attendance.domain.model;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class LeaveBalance {

    private final Long id;
    private final Long userId;
    private final BigDecimal totalDays;
    private final BigDecimal usedDays;

    private LeaveBalance(Long id, Long userId, BigDecimal totalDays, BigDecimal usedDays) {
        this.id = id;
        this.userId = userId;
        this.totalDays = totalDays;
        this.usedDays = usedDays;
    }

    public static LeaveBalance reconstitute(Long id, Long userId, BigDecimal totalDays, BigDecimal usedDays) {
        return new LeaveBalance(id, userId, totalDays, usedDays);
    }

    public static LeaveBalance createEmpty(Long userId) {
        return new LeaveBalance(null, userId, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public int remainingDays() {
        return totalDays.subtract(usedDays).intValue();
    }

    // total_days 자체가 "지금까지 적립된 개월 수"와 같다(월 1일 고정 적립).
    // 목표 경과 개월 수까지 부족한 만큼만 채워 넣고, 이미 그만큼 쌓여있으면 그대로 반환한다(멱등).
    public LeaveBalance accrueUpTo(int elapsedMonths) {
        int alreadyAccrued = totalDays.intValue();
        int toAdd = elapsedMonths - alreadyAccrued;
        if (toAdd <= 0) {
            return this;
        }
        return new LeaveBalance(id, userId, totalDays.add(BigDecimal.valueOf(toAdd)), usedDays);
    }
}