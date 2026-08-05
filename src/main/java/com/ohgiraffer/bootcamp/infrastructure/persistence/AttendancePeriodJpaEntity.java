package com.ohgiraffer.bootcamp.infrastructure.persistence;

import com.ohgiraffer.bootcamp.domain.model.AttendancePeriod;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "attendance_period")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttendancePeriodJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "period_no")
    private Integer periodNo;

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    @Column(name = "bootcamp_id", nullable = false)
    private Long bootcampId;

    private AttendancePeriodJpaEntity(Integer periodNo, LocalDate periodStart,
                                      LocalDate periodEnd, Long bootcampId) {
        this.periodNo = periodNo;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.bootcampId = bootcampId;
    }

    public static AttendancePeriodJpaEntity fromDomain(AttendancePeriod period) {
        return new AttendancePeriodJpaEntity(
                period.getPeriodNo(), period.getPeriodStart(),
                period.getPeriodEnd(), period.getBootcampId()
        );
    }

    public AttendancePeriod toDomain() {
        return AttendancePeriod.reconstruct(id, periodNo, periodStart, periodEnd, bootcampId);
    }
}