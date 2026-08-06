package com.ohgiraffer.submission.domain.model;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;

import java.time.Instant;

public final class SubmissionItemValue {

    private final Long id;
    private final Long submissionId;
    private final Long submissionBoxItemId;
    private final String fileKey;
    private final String originalFileName;
    private final String contentType;
    private final Long fileSize;
    private final String externalUrl;
    private final Instant createdAt;
    private final Instant updatedAt;
    private static final int MAX_ORIGINAL_FILE_NAME_LENGTH = 255;
    private static final int MAX_FILE_KEY_LENGTH = 500;

    private SubmissionItemValue(
            Long id,
            Long submissionId,
            Long submissionBoxItemId,
            String fileKey,
            String originalFileName,
            String contentType,
            Long fileSize,
            String externalUrl,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.submissionId = submissionId;
        this.submissionBoxItemId = submissionBoxItemId;
        this.fileKey = fileKey;
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.externalUrl = externalUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static SubmissionItemValue createFile(
            Long submissionBoxItemId,
            String fileKey,
            String originalFileName,
            String contentType,
            long fileSize
    ) {
        validateItemId(submissionBoxItemId);
        validateFileValue(
                fileKey,
                originalFileName,
                fileSize
        );

        return new SubmissionItemValue(
                null,
                null,
                submissionBoxItemId,
                fileKey,
                originalFileName.trim(),
                contentType,
                fileSize,
                null,
                null,
                null
        );
    }

    private static void validateFileValue(
            String fileKey,
            String originalFileName,
            long fileSize
    ) {
        if (fileKey == null || fileKey.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "파일 저장 키가 필요합니다."
            );
        }

        if (fileKey.length() > MAX_FILE_KEY_LENGTH) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "파일 저장 키는 500자 이하여야 합니다."
            );
        }

        if (originalFileName == null
                || originalFileName.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "원본 파일명이 필요합니다."
            );
        }

        if (originalFileName.trim().length()
                > MAX_ORIGINAL_FILE_NAME_LENGTH) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_FILE_NAME_TOO_LONG
            );
        }

        if (fileSize < 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "파일 크기가 올바르지 않습니다."
            );
        }
    }

    public static SubmissionItemValue createLink(
            Long submissionBoxItemId,
            String externalUrl
    ) {
        validateItemId(submissionBoxItemId);

        if (externalUrl == null || externalUrl.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "외부 링크가 필요합니다."
            );
        }

        if (externalUrl.trim().length() > 1000) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "외부 링크는 1000자 이하여야 합니다."
            );
        }

        return new SubmissionItemValue(
                null,
                null,
                submissionBoxItemId,
                null,
                null,
                null,
                null,
                externalUrl.trim(),
                null,
                null
        );
    }

    public static SubmissionItemValue restore(
            Long id,
            Long submissionId,
            Long submissionBoxItemId,
            String fileKey,
            String originalFileName,
            String contentType,
            Long fileSize,
            String externalUrl,
            Instant createdAt,
            Instant updatedAt
    ) {
        if (id == null || id <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "제출 항목 값 ID가 올바르지 않습니다."
            );
        }

        if (submissionId == null || submissionId <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "제출물 ID가 올바르지 않습니다."
            );
        }

        validateItemId(submissionBoxItemId);

        boolean fileValue = fileKey != null && !fileKey.isBlank();
        boolean linkValue = externalUrl != null && !externalUrl.isBlank();

        if (fileValue == linkValue) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "파일 또는 외부 링크 중 하나만 존재해야 합니다."
            );
        } if (fileValue) {
            validateFileValue(
                    fileKey,
                    originalFileName,
                    fileSize == null ? -1 : fileSize
            );
        }



        return new SubmissionItemValue(
                id,
                submissionId,
                submissionBoxItemId,
                fileKey,
                originalFileName,
                contentType,
                fileSize,
                externalUrl,
                createdAt,
                updatedAt
        );
    }

    private static void validateItemId(
            Long submissionBoxItemId
    ) {
        if (submissionBoxItemId == null
                || submissionBoxItemId <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "제출함 항목 ID가 올바르지 않습니다."
            );
        }
    }

    public Long getId() {
        return id;
    }

    public Long getSubmissionId() {
        return submissionId;
    }

    public Long getSubmissionBoxItemId() {
        return submissionBoxItemId;
    }

    public String getFileKey() {
        return fileKey;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}