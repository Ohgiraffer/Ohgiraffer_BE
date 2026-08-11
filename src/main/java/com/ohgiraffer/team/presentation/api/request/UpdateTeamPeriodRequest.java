package com.ohgiraffer.team.presentation.api.request;

import com.ohgiraffer.team.application.command.UpdateTeamPeriodCommand;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateTeamPeriodRequest(
        @NotNull(message = "팀 기간 시작일을 입력해주세요.")
        LocalDate startDate,

        @NotNull(message = "팀 기간 종료일을 입력해주세요.")
        LocalDate endDate
) {

    public UpdateTeamPeriodCommand toCommand(
            Long requesterId,
            Long teamPeriodId
    ) {
        return new UpdateTeamPeriodCommand(
                requesterId,
                teamPeriodId,
                startDate,
                endDate
        );
    }
}