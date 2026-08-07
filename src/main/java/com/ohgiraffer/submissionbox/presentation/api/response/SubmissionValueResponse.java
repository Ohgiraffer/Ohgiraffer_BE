package com.ohgiraffer.submissionbox.presentation.api.response;

import com.ohgiraffer.submissionbox.application.usecase.SubmissionValueResult;
import com.ohgiraffer.submissionbox.domain.model.SubmissionItemType;

public record SubmissionValueResponse(
        Long submissionItemValueId,
        Long submissionBoxItemId,
        SubmissionItemType itemType,
        String originalFileName,
        String contentType,
        Long fileSize,
        String externalUrl
) {

    public static SubmissionValueResponse from(
            SubmissionValueResult result
    ) {
        return new SubmissionValueResponse(
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