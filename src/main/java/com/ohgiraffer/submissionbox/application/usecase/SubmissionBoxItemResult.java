package com.ohgiraffer.submissionbox.application.usecase;

import com.ohgiraffer.submissionbox.domain.model.SubmissionBoxItem;
import com.ohgiraffer.submissionbox.domain.model.SubmissionItemType;

public record SubmissionBoxItemResult(
        Long submissionBoxItemId,
        String itemName,
        SubmissionItemType itemType,
        String allowedFileTypes,
        boolean required,
        int sortOrder
) {

    public static SubmissionBoxItemResult from(
            SubmissionBoxItem item
    ) {
        return new SubmissionBoxItemResult(
                item.getId(),
                item.getItemName(),
                item.getItemType(),
                item.getAllowedFileTypes(),
                item.isRequired(),
                item.getSortOrder()
        );
    }
}