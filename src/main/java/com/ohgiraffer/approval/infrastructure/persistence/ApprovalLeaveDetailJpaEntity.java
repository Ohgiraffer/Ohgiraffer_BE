package com.ohgiraffer.approval.infrastructure.persistence;

import com.ohgiraffer.approval.domain.model.approval.ApprovalLeaveDetail;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "approval_leave_detail")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApprovalLeaveDetailJpaEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(name = "leave_detail_id")
    private Long id;

    @Column(
            name = "approval_id",
            nullable = false
    )
    private Long approvalId;

    @Column(
            name = "start_date",
            nullable = false
    )
    private LocalDate startDate;

    @Column(
            name = "end_date",
            nullable = false
    )
    private LocalDate endDate;

    private ApprovalLeaveDetailJpaEntity(
            Long id,
            Long approvalId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        this.id = id;
        this.approvalId = approvalId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static ApprovalLeaveDetailJpaEntity from(
            ApprovalLeaveDetail approvalLeaveDetail
    ) {
        return new ApprovalLeaveDetailJpaEntity(
                approvalLeaveDetail.getId(),
                approvalLeaveDetail.getApprovalId(),
                approvalLeaveDetail.getStartDate(),
                approvalLeaveDetail.getEndDate()
        );
    }

    public ApprovalLeaveDetail toDomain() {
        return ApprovalLeaveDetail.restore(
                id,
                approvalId,
                startDate,
                endDate
        );
    }
}