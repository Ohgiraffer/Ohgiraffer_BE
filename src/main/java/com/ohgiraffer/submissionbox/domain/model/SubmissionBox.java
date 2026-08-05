package com.ohgiraffer.submissionbox.domain.model;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public final class SubmissionBox {

    private final Long id;
    private final String projectName;
    private final SubmissionTargetScope targetScope;
    private final LocalDateTime startAt;
    private final LocalDateTime dueAt;
    private final LatePolicy latePolicy;
    private final Long createdBy;
    private final List<SubmissionBoxItem> items;
    private final Instant createdAt;
    private final Instant updatedAt;

    private SubmissionBox(
            Long id,
            String projectName,
            SubmissionTargetScope targetScope,
            LocalDateTime startAt,
            LocalDateTime dueAt,
            LatePolicy latePolicy,
            Long createdBy,
            List<SubmissionBoxItem> items,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.projectName = projectName;
        this.targetScope = targetScope;
        this.startAt = startAt;
        this.dueAt = dueAt;
        this.latePolicy = latePolicy;
        this.createdBy = createdBy;
        this.items = List.copyOf(items);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static SubmissionBox create(
            String projectName,
            SubmissionTargetScope targetScope,
            LocalDateTime startAt,
            LocalDateTime dueAt,
            LatePolicy latePolicy,
            Long createdBy,
            List<SubmissionBoxItem> items
    ) {
        validate(
                projectName,
                targetScope,
                startAt,
                dueAt,
                latePolicy,
                createdBy,
                items
        );

        return new SubmissionBox(
                null,
                projectName.trim(),
                targetScope,
                startAt,
                dueAt,
                latePolicy,
                createdBy,
                items,
                null,
                null
        );
    }

    public static SubmissionBox restore(
            Long id,
            String projectName,
            SubmissionTargetScope targetScope,
            LocalDateTime startAt,
            LocalDateTime dueAt,
            LatePolicy latePolicy,
            Long createdBy,
            List<SubmissionBoxItem> items,
            Instant createdAt,
            Instant updatedAt
    ) {
        if (id == null || id <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "제출함 ID가 올바르지 않습니다."
            );
        }

        validate(
                projectName,
                targetScope,
                startAt,
                dueAt,
                latePolicy,
                createdBy,
                items
        );

        return new SubmissionBox(
                id,
                projectName.trim(),
                targetScope,
                startAt,
                dueAt,
                latePolicy,
                createdBy,
                items,
                createdAt,
                updatedAt
        );
    }

    private static void validate(
            String projectName,
            SubmissionTargetScope targetScope,
            LocalDateTime startAt,
            LocalDateTime dueAt,
            LatePolicy latePolicy,
            Long createdBy,
            List<SubmissionBoxItem> items
    ) {
        if (projectName == null || projectName.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "프로젝트명은 필수입니다."
            );
        }

        if (projectName.trim().length() > 255) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "프로젝트명은 255자 이하여야 합니다."
            );
        }

        if (targetScope == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "제출 단위는 필수입니다."
            );
        }

        if (startAt == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "제출 시작 일시는 필수입니다."
            );
        }

        if (dueAt == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "제출 마감 일시는 필수입니다."
            );
        }

        if (startAt.isAfter(dueAt)) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "제출 시작 일시는 마감 일시보다 늦을 수 없습니다."
            );
        }

        if (latePolicy == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "지각 제출 정책은 필수입니다."
            );
        }

        if (createdBy == null || createdBy <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "생성자 ID가 올바르지 않습니다."
            );
        }

        if (items == null || items.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "제출 항목을 최소 1개 이상 등록해야 합니다."
            );
        }

        if (items.stream().anyMatch(item -> item == null)) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "제출 항목이 올바르지 않습니다."
            );
        }

        long distinctSortOrderCount = items.stream()
                .map(SubmissionBoxItem::getSortOrder)
                .distinct()
                .count();

        if (distinctSortOrderCount != items.size()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "제출 항목 순서는 중복될 수 없습니다."
            );
        }
    }

    public Long getId() {
        return id;
    }

    public String getProjectName() {
        return projectName;
    }

    public SubmissionTargetScope getTargetScope() {
        return targetScope;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public LocalDateTime getDueAt() {
        return dueAt;
    }

    public LatePolicy getLatePolicy() {
        return latePolicy;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public List<SubmissionBoxItem> getItems() {
        return items;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}