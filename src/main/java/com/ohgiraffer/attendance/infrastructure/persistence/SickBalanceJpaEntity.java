package com.ohgiraffer.attendance.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "sick_balance")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SickBalanceJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sick_balance_id")
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "total_days", nullable = false, precision = 5, scale = 1)
    private BigDecimal totalDays;

    @Column(name = "used_days", nullable = false, precision = 5, scale = 1)
    private BigDecimal usedDays;

    private SickBalanceJpaEntity(Long id, Long userId, BigDecimal totalDays, BigDecimal usedDays) {
        this.id = id;
        this.userId = userId;
        this.totalDays = totalDays;
        this.usedDays = usedDays;
    }

    public static SickBalanceJpaEntity of(Long userId, BigDecimal totalDays, BigDecimal usedDays) {
        return new SickBalanceJpaEntity(null, userId, totalDays, usedDays);
    }

    public static SickBalanceJpaEntity reconstitute(Long id, Long userId, BigDecimal totalDays, BigDecimal usedDays) {
        return new SickBalanceJpaEntity(id, userId, totalDays, usedDays);
    }
}