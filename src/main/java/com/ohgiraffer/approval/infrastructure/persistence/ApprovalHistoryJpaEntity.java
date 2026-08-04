package com.ohgiraffer.approval.infrastructure.persistence;

import com.ohgiraffer.approval.domain.model.approval.ApprovalHistory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "approval_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApprovalHistoryJpaEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(name = "approval_history_id")
    private Long id;

    @Column(
            name = "approval_id",
            nullable = false
    )
    private Long approvalId;

    @Column(name = "changed_by")
    private Long changedBy;

    @Column(
            name = "field_name",
            nullable = false,
            length = 30
    )
    private String fieldName;

    @Column(
            name = "old_value",
            length = 30
    )
    private String oldValue;

    @Column(
            name = "new_value",
            length = 30
    )
    private String newValue;

    @Column(name = "note")
    private String note;

    @Column(
            name = "changed_at",
            nullable = false
    )
    private LocalDateTime changedAt;

    private ApprovalHistoryJpaEntity(
            Long id,
            Long approvalId,
            Long changedBy,
            String fieldName,
            String oldValue,
            String newValue,
            String note,
            LocalDateTime changedAt
    ) {
        this.id = id;
        this.approvalId = approvalId;
        this.changedBy = changedBy;
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.note = note;
        this.changedAt = changedAt;
    }

    public static ApprovalHistoryJpaEntity from(
            ApprovalHistory approvalHistory
    ) {
        return new ApprovalHistoryJpaEntity(
                approvalHistory.getId(),
                approvalHistory.getApprovalId(),
                approvalHistory.getChangedBy(),
                approvalHistory.getFieldName(),
                approvalHistory.getOldValue(),
                approvalHistory.getNewValue(),
                approvalHistory.getNote(),
                approvalHistory.getChangedAt()
        );
    }

    public ApprovalHistory toDomain() {
        return ApprovalHistory.restore(
                id,
                approvalId,
                changedBy,
                fieldName,
                oldValue,
                newValue,
                note,
                changedAt
        );
    }
}