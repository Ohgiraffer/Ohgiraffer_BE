package com.ohgiraffer.space.presentation.api.request;

import com.ohgiraffer.space.application.command.CreateSpaceCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateSpaceRequest(

        @NotBlank(
                message = "공간명은 필수입니다."
        )
        @Size(
                max = 100,
                message = "공간명은 100자 이하여야 합니다."
        )
        String spaceName,

        @NotNull(
                message = "최대 수용 인원은 필수입니다."
        )
        @Min(
                value = 1,
                message = "최대 수용 인원은 1명 이상이어야 합니다."
        )
        Integer capacity
) {

    public CreateSpaceCommand toCommand() {
        return new CreateSpaceCommand(
                spaceName,
                capacity
        );
    }
}