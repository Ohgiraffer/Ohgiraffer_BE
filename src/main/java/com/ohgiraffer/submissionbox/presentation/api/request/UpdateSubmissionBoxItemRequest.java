package com.ohgiraffer.submissionbox.presentation.api.request;

import com.ohgiraffer.submissionbox.application.command.UpdateSubmissionBoxItemCommand;
import com.ohgiraffer.submissionbox.domain.model.SubmissionItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateSubmissionBoxItemRequest(

        @Positive(message = "제출 항목 ID는 양수여야 합니다.")
        Long submissionBoxItemId,

        @NotBlank(message = "제출 항목명은 필수입니다.")
        @Size(
                max = 100,
                message = "제출 항목명은 100자 이하여야 합니다."
        )
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

    public UpdateSubmissionBoxItemCommand toCommand(
            int sortOrder
    ) {
        return new UpdateSubmissionBoxItemCommand(
                submissionBoxItemId,
                itemName,
                itemType,
                allowedFileTypes,
                required,
                sortOrder
        );
    }
}