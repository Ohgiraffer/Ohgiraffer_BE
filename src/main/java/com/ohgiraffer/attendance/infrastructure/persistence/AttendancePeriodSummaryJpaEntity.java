package com.ohgiraffer.attendance.infrastructure.persistence;

import com.ohgiraffer.attendance.domain.model.AttendanceRiskLevel;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "attendance_period_summary")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttendancePeriodSummaryJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "present_days")
    private Integer presentDays;

    @Column(name = "late_count")
    private Integer lateCount;

    @Column(name = "early_leave_count")
    private Integer earlyLeaveCount;

    @Column(name = "absent_days")
    private Integer absentDays;

    @Column(name = "outing_count")
    private Integer outingCount;

    @Column(name = "leave_days")
    private Integer leaveDays;

    @Column(name = "sick_days")
    private Integer sickDays;

    @Column(name = "attendance_rate")
    private BigDecimal attendanceRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level")
    private AttendanceRiskLevel riskLevel;

    @Column(name = "period_id", nullable = false)
    private Long periodId;
}