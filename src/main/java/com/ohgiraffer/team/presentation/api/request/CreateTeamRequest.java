package com.ohgiraffer.team.presentation.api.request;

import com.ohgiraffer.team.application.command.CreateTeamCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateTeamRequest(
        @NotBlank(message = "팀명을 입력해주세요.")
        @Size(max = 100, message = "팀명은 100자를 초과할 수 없습니다.")
        String name,

        @NotNull(message = "팀 시작일을 입력해주세요.")
        LocalDate startDate,

        @NotNull(message = "팀 종료일을 입력해주세요.")
        LocalDate endDate
) {

    public CreateTeamCommand toCommand(
            Long requesterId
    ) {
        return new CreateTeamCommand(
                requesterId,
                name,
                startDate,
                endDate
        );
    }
}