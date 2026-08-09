package com.ohgiraffer.attendance.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "leave_balance")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LeaveBalanceJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "leave_balance_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "total_days", nullable = false, precision = 4, scale = 1)
    private BigDecimal totalDays;

    @Column(name = "used_days", nullable = false, precision = 4, scale = 1)
    private BigDecimal usedDays;

    @Column(name = "carried_over_days", nullable = false, precision = 4, scale = 1)
    private BigDecimal carriedOverDays;

    private LeaveBalanceJpaEntity(Long id, Long userId, LocalDate periodStart, LocalDate periodEnd,
                                  BigDecimal totalDays, BigDecimal usedDays, BigDecimal carriedOverDays) {
        this.id = id;
        this.userId = userId;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.totalDays = totalDays;
        this.usedDays = usedDays;
        this.carriedOverDays = carriedOverDays;
    }

    // 신규 생성용
    public static LeaveBalanceJpaEntity of(Long userId, LocalDate periodStart, LocalDate periodEnd,
                                           BigDecimal totalDays, BigDecimal usedDays, BigDecimal carriedOverDays) {
        return new LeaveBalanceJpaEntity(null, userId, periodStart, periodEnd, totalDays, usedDays, carriedOverDays);
    }

    // 수정용
    public static LeaveBalanceJpaEntity reconstitute(Long id, Long userId, LocalDate periodStart, LocalDate periodEnd,
                                                     BigDecimal totalDays, BigDecimal usedDays, BigDecimal carriedOverDays) {
        return new LeaveBalanceJpaEntity(id, userId, periodStart, periodEnd, totalDays, usedDays, carriedOverDays);
    }
}