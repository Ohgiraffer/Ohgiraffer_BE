package com.ohgiraffer.submissionbox.presentation.api.request;

import com.ohgiraffer.submissionbox.application.command.CreateSubmissionBoxItemCommand;
import com.ohgiraffer.submissionbox.domain.model.SubmissionItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateSubmissionBoxItemRequest(

        @NotBlank(message = "제출 항목명은 필수입니다.")
        @Size(max = 100, message = "제출 항목명은 100자 이하여야 합니다.")
        String itemName,

        @NotNull(message = "제출 항목 유형은 필수입니다.")
        SubmissionItemType itemType,

        @Size(
                max = 255,
                message = "허용 파일 형식은 255자 이하여야 합니다."
        )
        String allowedFileTypes,

        boolean required
) {

    public CreateSubmissionBoxItemCommand toCommand(
            int sortOrder
    ) {
        return new CreateSubmissionBoxItemCommand(
                itemName,
                itemType,
                allowedFileTypes,
                required,
                sortOrder
        );
    }
}