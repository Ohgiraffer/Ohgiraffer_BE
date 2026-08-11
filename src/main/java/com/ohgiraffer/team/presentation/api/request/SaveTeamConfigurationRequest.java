package com.ohgiraffer.team.presentation.api.request;

import com.ohgiraffer.team.application.command.SaveTeamConfigurationCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record SaveTeamConfigurationRequest(
        @NotNull(message = "팀 기간 ID를 입력해주세요.")
        @Positive(message = "팀 기간 ID가 올바르지 않습니다.")
        Long teamPeriodId,

        @NotNull(message = "팀 구성 목록을 입력해주세요.")
        List<@Valid @NotNull(message = "팀 구성 정보를 입력해주세요.") TeamConfigurationRequest> teams,

        List<@Positive(message = "팀 ID가 올바르지 않습니다.") Long> deletedTeamIds,

        List<@Positive(message = "사용자 ID가 올바르지 않습니다.") Long> unassignedUserIds
) {

    public SaveTeamConfigurationCommand toCommand(
            Long requesterId
    ) {
        return new SaveTeamConfigurationCommand(
                requesterId,
                teamPeriodId,
                teams.stream()
                        .map(TeamConfigurationRequest::toCommand)
                        .toList(),
                deletedTeamIds,
                unassignedUserIds
        );
    }
}