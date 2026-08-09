package com.ohgiraffer.team.presentation.api.request;

import com.ohgiraffer.team.application.command.SaveTeamAssignmentsCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SaveTeamAssignmentsRequest(
        @NotNull(message = "팀 배정 정보를 입력해주세요.")
        @Valid
        List<TeamAssignmentRequest> teams,

        List<Long> unassignedUserIds
) {

    public SaveTeamAssignmentsCommand toCommand(
            Long requesterId
    ) {
        return new SaveTeamAssignmentsCommand(
                requesterId,
                teams.stream()
                        .map(TeamAssignmentRequest::toCommand)
                        .toList(),
                unassignedUserIds == null ? List.of() : unassignedUserIds
        );
    }
}