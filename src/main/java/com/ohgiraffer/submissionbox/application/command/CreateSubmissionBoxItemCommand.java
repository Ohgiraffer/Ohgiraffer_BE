package com.ohgiraffer.submissionbox.application.command;

import com.ohgiraffer.submissionbox.domain.model.SubmissionItemType;

public record CreateSubmissionBoxItemCommand(
        String itemName,
        SubmissionItemType itemType,
        String allowedFileTypes,
        boolean required,
        int sortOrder
) {
}