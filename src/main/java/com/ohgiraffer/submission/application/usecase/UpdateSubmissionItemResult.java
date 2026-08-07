package com.ohgiraffer.submission.application.usecase;

import com.ohgiraffer.submission.domain.model.SubmissionItemValue;
import com.ohgiraffer.submissionbox.domain.model.SubmissionItemType;

public record UpdateSubmissionItemResult(
        Long submissionItemValueId,
        Long submissionBoxItemId,
        SubmissionItemType itemType,
        String originalFileName,
        String contentType,
        Long fileSize,
        String externalUrl
) {

    public static UpdateSubmissionItemResult from(
            SubmissionItemValue value
    ) {
        SubmissionItemType itemType =
                value.getFileKey() != null
                        && !value.getFileKey().isBlank()
                        ? SubmissionItemType.FILE
                        : SubmissionItemType.LINK;

        return new UpdateSubmissionItemResult(
                value.getId(),
                value.getSubmissionBoxItemId(),
                itemType,
                value.getOriginalFileName(),
                value.getContentType(),
                value.getFileSize(),
                value.getExternalUrl()
        );
    }
}