package com.ohgiraffer.submissionbox.application.usecase;

import com.ohgiraffer.submission.domain.model.SubmissionItemValue;
import com.ohgiraffer.submissionbox.domain.model.SubmissionItemType;

public record SubmissionValueResult(
        Long submissionItemValueId,
        Long submissionBoxItemId,
        SubmissionItemType itemType,
        String originalFileName,
        String contentType,
        Long fileSize,
        String externalUrl
) {

    public static SubmissionValueResult from(
            SubmissionItemValue value
    ) {
        SubmissionItemType itemType =
                isFile(value)
                        ? SubmissionItemType.FILE
                        : SubmissionItemType.LINK;

        return new SubmissionValueResult(
                value.getId(),
                value.getSubmissionBoxItemId(),
                itemType,
                value.getOriginalFileName(),
                value.getContentType(),
                value.getFileSize(),
                value.getExternalUrl()
        );
    }

    private static boolean isFile(
            SubmissionItemValue value
    ) {
        return value.getFileKey() != null
                && !value.getFileKey().isBlank();
    }
}