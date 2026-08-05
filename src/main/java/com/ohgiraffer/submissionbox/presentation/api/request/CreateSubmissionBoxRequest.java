package com.ohgiraffer.submissionbox.presentation.api.request;

import com.ohgiraffer.submissionbox.application.command.CreateSubmissionBoxCommand;
import com.ohgiraffer.submissionbox.application.command.CreateSubmissionBoxItemCommand;
import com.ohgiraffer.submissionbox.domain.model.LatePolicy;
import com.ohgiraffer.submissionbox.domain.model.SubmissionTargetScope;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

public record CreateSubmissionBoxRequest(

        @NotBlank(message = "프로젝트명은 필수입니다.")
        @Size(max = 255, message = "프로젝트명은 255자 이하여야 합니다.")
        String projectName,

        @NotNull(message = "제출 단위는 필수입니다.")
        SubmissionTargetScope targetScope,

        @NotNull(message = "제출 시작 일시는 필수입니다.")
        LocalDateTime startAt,

        @NotNull(message = "제출 마감 일시는 필수입니다.")
        LocalDateTime dueAt,

        @NotNull(message = "지각 제출 정책은 필수입니다.")
        LatePolicy latePolicy,

        @Valid
        @NotEmpty(message = "제출 항목을 최소 1개 이상 등록해야 합니다.")
        List<CreateSubmissionBoxItemRequest> items
) {

    public CreateSubmissionBoxCommand toCommand(
            Long createdBy
    ) {
        List<CreateSubmissionBoxItemCommand> itemCommands =
                IntStream.range(0, items.size())
                        .mapToObj(index ->
                                items.get(index)
                                        .toCommand(index + 1)
                        )
                        .toList();

        return new CreateSubmissionBoxCommand(
                projectName,
                targetScope,
                startAt,
                dueAt,
                latePolicy,
                createdBy,
                itemCommands
        );
    }
}