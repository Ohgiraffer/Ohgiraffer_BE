package com.ohgiraffer.submissionbox.presentation.api.response;

import com.ohgiraffer.submissionbox.application.usecase.SubmissionBoxItemResult;
import com.ohgiraffer.submissionbox.domain.model.SubmissionItemType;

public record SubmissionBoxItemResponse(
        Long submissionBoxItemId,
        String itemName,
        SubmissionItemType itemType,
        String allowedFileTypes,
        boolean required,
        int sortOrder
) {

    public static SubmissionBoxItemResponse from(
            SubmissionBoxItemResult result
    ) {
        return new SubmissionBoxItemResponse(
                result.submissionBoxItemId(),
                result.itemName(),
                result.itemType(),
                result.allowedFileTypes(),
                result.required(),
                result.sortOrder()
        );
    }
}