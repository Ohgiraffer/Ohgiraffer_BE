package com.ohgiraffer.submission.presentation.api.response;

import com.ohgiraffer.submission.application.usecase.UpdateSubmissionItemResult;
import com.ohgiraffer.submissionbox.domain.model.SubmissionItemType;

public record UpdateSubmissionItemResponse(
        Long submissionItemValueId,
        Long submissionBoxItemId,
        SubmissionItemType itemType,
        String originalFileName,
        String contentType,
        Long fileSize,
        String externalUrl
) {

    public static UpdateSubmissionItemResponse from(
            UpdateSubmissionItemResult result
    ) {
        return new UpdateSubmissionItemResponse(
                result.submissionItemValueId(),
                result.submissionBoxItemId(),
                result.itemType(),
                result.originalFileName(),
                result.contentType(),
                result.fileSize(),
                result.externalUrl()
        );
    }
}