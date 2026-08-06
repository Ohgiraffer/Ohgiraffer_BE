package com.ohgiraffer.submissionbox.application.command;

import com.ohgiraffer.submissionbox.domain.model.SubmissionItemType;

public record UpdateSubmissionBoxItemCommand(
        Long submissionBoxItemId,
        String itemName,
        SubmissionItemType itemType,
        String allowedFileTypes,
        boolean required,
        int sortOrder
) {
}