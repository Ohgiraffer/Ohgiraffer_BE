package com.ohgiraffer.submissionbox.domain.model;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;

import java.time.Instant;

public final class SubmissionBoxItem {

    private final Long id;
    private final Long submissionBoxId;
    private final String itemName;
    private final SubmissionItemType itemType;
    private final String allowedFileTypes;
    private final boolean required;
    private final int sortOrder;
    private final Instant createdAt;
    private final Instant updatedAt;

    private SubmissionBoxItem(
            Long id,
            Long submissionBoxId,
            String itemName,
            SubmissionItemType itemType,
            String allowedFileTypes,
            boolean required,
            int sortOrder,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.submissionBoxId = submissionBoxId;
        this.itemName = itemName;
        this.itemType = itemType;
        this.allowedFileTypes = allowedFileTypes;
        this.required = required;
        this.sortOrder = sortOrder;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static SubmissionBoxItem create(
            String itemName,
            SubmissionItemType itemType,
            String allowedFileTypes,
            boolean required,
            int sortOrder
    ) {
        validate(
                itemName,
                itemType,
                allowedFileTypes,
                sortOrder
        );

        return new SubmissionBoxItem(
                null,
                null,
                itemName.trim(),
                itemType,
                normalizeAllowedFileTypes(
                        itemType,
                        allowedFileTypes
                ),
                required,
                sortOrder,
                null,
                null
        );
    }

    public static SubmissionBoxItem restore(
            Long id,
            Long submissionBoxId,
            String itemName,
            SubmissionItemType itemType,
            String allowedFileTypes,
            boolean required,
            int sortOrder,
            Instant createdAt,
            Instant updatedAt
    ) {
        if (id == null || id <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "제출 항목 ID가 올바르지 않습니다."
            );
        }

        if (submissionBoxId == null || submissionBoxId <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "제출함 ID가 올바르지 않습니다."
            );
        }

        validate(
                itemName,
                itemType,
                allowedFileTypes,
                sortOrder
        );

        return new SubmissionBoxItem(
                id,
                submissionBoxId,
                itemName.trim(),
                itemType,
                normalizeAllowedFileTypes(
                        itemType,
                        allowedFileTypes
                ),
                required,
                sortOrder,
                createdAt,
                updatedAt
        );
    }

    public SubmissionBoxItem update(
            String itemName,
            SubmissionItemType itemType,
            String allowedFileTypes,
            boolean required,
            int sortOrder
    ) {
        validate(
                itemName,
                itemType,
                allowedFileTypes,
                sortOrder
        );

        return new SubmissionBoxItem(
                id,
                submissionBoxId,
                itemName.trim(),
                itemType,
                normalizeAllowedFileTypes(
                        itemType,
                        allowedFileTypes
                ),
                required,
                sortOrder,
                createdAt,
                updatedAt
        );
    }

    private static void validate(
            String itemName,
            SubmissionItemType itemType,
            String allowedFileTypes,
            int sortOrder
    ) {
        if (itemName == null || itemName.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "제출 항목명은 필수입니다."
            );
        }

        if (itemName.trim().length() > 100) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "제출 항목명은 100자 이하여야 합니다."
            );
        }

        if (itemType == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "제출 항목 유형은 필수입니다."
            );
        }

        if (itemType == SubmissionItemType.FILE
                && (allowedFileTypes == null
                || allowedFileTypes.isBlank())) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "파일 제출 항목은 허용 파일 형식이 필요합니다."
            );
        }

        if (allowedFileTypes != null
                && allowedFileTypes.trim().length() > 255) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "허용 파일 형식은 255자 이하여야 합니다."
            );
        }

        if (sortOrder <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "제출 항목 순서는 1 이상이어야 합니다."
            );
        }
    }

    private static String normalizeAllowedFileTypes(
            SubmissionItemType itemType,
            String allowedFileTypes
    ) {
        if (itemType == SubmissionItemType.LINK) {
            return null;
        }

        return allowedFileTypes.trim();
    }

    public Long getId() {
        return id;
    }

    public Long getSubmissionBoxId() {
        return submissionBoxId;
    }

    public String getItemName() {
        return itemName;
    }

    public SubmissionItemType getItemType() {
        return itemType;
    }

    public String getAllowedFileTypes() {
        return allowedFileTypes;
    }

    public boolean isRequired() {
        return required;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}