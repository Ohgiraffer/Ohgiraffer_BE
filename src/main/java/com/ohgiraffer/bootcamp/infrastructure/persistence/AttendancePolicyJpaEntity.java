package com.ohgiraffer.bootcamp.infrastructure.persistence;

import com.ohgiraffer.bootcamp.domain.model.AttendancePolicy;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "attendance_policy")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttendancePolicyJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_policy_id")
    private Long id;

    @Column(name = "caution_threshold_pct")
    private BigDecimal cautionThresholdPct;

    @Column(name = "warning_threshold_pct")
    private BigDecimal warningThresholdPct;

    @Column(name = "period_expulsion_pct")
    private BigDecimal periodExpulsionPct;

    @Column(name = "bootcamp_id", nullable = false)
    private Long bootcampId;

    private AttendancePolicyJpaEntity(BigDecimal cautionThresholdPct, BigDecimal warningThresholdPct,
                                      BigDecimal periodExpulsionPct, Long bootcampId) {
        this.cautionThresholdPct = cautionThresholdPct;
        this.warningThresholdPct = warningThresholdPct;
        this.periodExpulsionPct = periodExpulsionPct;
        this.bootcampId = bootcampId;
    }

    public static AttendancePolicyJpaEntity fromDomain(AttendancePolicy policy) {
        return new AttendancePolicyJpaEntity(
                policy.getCautionThresholdPct(),
                policy.getWarningThresholdPct(),
                policy.getPeriodExpulsionPct(),
                policy.getBootcampId()
        );
    }

    public AttendancePolicy toDomain() {
        return AttendancePolicy.reconstruct(id, cautionThresholdPct, warningThresholdPct,
                periodExpulsionPct, bootcampId);
    }

}
