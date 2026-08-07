package com.ohgiraffer.submission.domain.model;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public final class Submission {

    private final Long id;
    private final Long submissionBoxId;
    private final Long ownerUserId;
    private final Long teamId;
    private final Long submittedBy;
    private final LocalDateTime submittedAt;
    private final boolean late;
    private final List<SubmissionItemValue> itemValues;
    private final Instant createdAt;
    private final Instant updatedAt;

    private Submission(
            Long id,
            Long submissionBoxId,
            Long ownerUserId,
            Long teamId,
            Long submittedBy,
            LocalDateTime submittedAt,
            boolean late,
            List<SubmissionItemValue> itemValues,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.submissionBoxId = submissionBoxId;
        this.ownerUserId = ownerUserId;
        this.teamId = teamId;
        this.submittedBy = submittedBy;
        this.submittedAt = submittedAt;
        this.late = late;
        this.itemValues = List.copyOf(itemValues);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Submission create(
            Long submissionBoxId,
            Long ownerUserId,
            Long teamId,
            Long submittedBy,
            LocalDateTime submittedAt,
            boolean late,
            List<SubmissionItemValue> itemValues
    ) {
        validate(
                submissionBoxId,
                ownerUserId,
                teamId,
                submittedBy,
                submittedAt,
                itemValues
        );

        return new Submission(
                null,
                submissionBoxId,
                ownerUserId,
                teamId,
                submittedBy,
                submittedAt,
                late,
                itemValues,
                null,
                null
        );
    }

    public static Submission restore(
            Long id,
            Long submissionBoxId,
            Long ownerUserId,
            Long teamId,
            Long submittedBy,
            LocalDateTime submittedAt,
            boolean late,
            List<SubmissionItemValue> itemValues,
            Instant createdAt,
            Instant updatedAt
    ) {
        if (id == null || id <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "제출물 ID가 올바르지 않습니다."
            );
        }

        validate(
                submissionBoxId,
                ownerUserId,
                teamId,
                submittedBy,
                submittedAt,
                itemValues
        );

        return new Submission(
                id,
                submissionBoxId,
                ownerUserId,
                teamId,
                submittedBy,
                submittedAt,
                late,
                itemValues,
                createdAt,
                updatedAt
        );
    }

    private static void validate(
            Long submissionBoxId,
            Long ownerUserId,
            Long teamId,
            Long submittedBy,
            LocalDateTime submittedAt,
            List<SubmissionItemValue> itemValues
    ) {
        if (submissionBoxId == null || submissionBoxId <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "제출함 ID가 올바르지 않습니다."
            );
        }

        boolean individualOwner =
                ownerUserId != null && teamId == null;

        boolean teamOwner =
                ownerUserId == null && teamId != null;

        if (!individualOwner && !teamOwner) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "개인 제출자 또는 제출 팀 중 하나만 지정해야 합니다."
            );
        }

        if (submittedBy == null || submittedBy <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "실제 제출자 ID가 올바르지 않습니다."
            );
        }

        if (submittedAt == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "제출 시각이 필요합니다."
            );
        }

        if (itemValues == null || itemValues.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "제출 항목이 필요합니다."
            );
        }

        if (itemValues.stream().anyMatch(value -> value == null)) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "제출 항목에 빈 값이 포함될 수 없습니다."
            );
        }

        long distinctItemCount = itemValues.stream()
                .map(SubmissionItemValue::getSubmissionBoxItemId)
                .distinct()
                .count();

        if (distinctItemCount != itemValues.size()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "동일한 제출 항목을 중복 제출할 수 없습니다."
            );
        }
    }

    public Submission resubmit(
            Long submittedBy,
            LocalDateTime submittedAt,
            List<SubmissionItemValue> itemValues
    ) {
        validate(
                submissionBoxId,
                ownerUserId,
                teamId,
                submittedBy,
                submittedAt,
                itemValues
        );

        return new Submission(
                id,
                submissionBoxId,
                ownerUserId,
                teamId,
                submittedBy,
                submittedAt,
                false,
                itemValues,
                createdAt,
                updatedAt
        );
    }

    public Long getId() {
        return id;
    }

    public Long getSubmissionBoxId() {
        return submissionBoxId;
    }

    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public Long getTeamId() {
        return teamId;
    }

    public Long getSubmittedBy() {
        return submittedBy;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public boolean isLate() {
        return late;
    }

    public List<SubmissionItemValue> getItemValues() {
        return itemValues;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}