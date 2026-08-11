package com.ohgiraffer.team.presentation.api.request;

import com.ohgiraffer.team.application.command.CreateTeamPeriodCommand;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateTeamPeriodRequest(
        @NotNull(message = "팀 기간 시작일을 입력해주세요.")
        LocalDate startDate,

        @NotNull(message = "팀 기간 종료일을 입력해주세요.")
        LocalDate endDate
) {

    public CreateTeamPeriodCommand toCommand(
            Long requesterId
    ) {
        return new CreateTeamPeriodCommand(
                requesterId,
                startDate,
                endDate
        );
    }
}