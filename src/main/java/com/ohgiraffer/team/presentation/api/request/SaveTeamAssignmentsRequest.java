package com.ohgiraffer.team.presentation.api.request;

import com.ohgiraffer.team.application.command.SaveTeamAssignmentsCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record SaveTeamAssignmentsRequest(
        @NotNull(message = "팀 구성 목록을 입력해주세요.")
        List<@Valid @NotNull(message = "팀 구성 정보를 입력해주세요.") TeamAssignmentRequest> teams,

        List<@Positive(message = "사용자 ID가 올바르지 않습니다.") Long> unassignedUserIds
) {

    public SaveTeamAssignmentsCommand toCommand(
            Long requesterId
    ) {
        return new SaveTeamAssignmentsCommand(
                requesterId,
                teams.stream()
                        .map(TeamAssignmentRequest::toCommand)
                        .toList(),
                unassignedUserIds
        );
    }
}