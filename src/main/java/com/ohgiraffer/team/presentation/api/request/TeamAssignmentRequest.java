package com.ohgiraffer.team.presentation.api.request;

import com.ohgiraffer.team.application.command.TeamAssignmentCommand;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record TeamAssignmentRequest(
        @NotNull(message = "팀 ID를 입력해주세요.")
        @Positive(message = "팀 ID가 올바르지 않습니다.")
        Long teamId,

        List<@NotNull(message = "사용자 ID를 입력해주세요.")
        @Positive(message = "사용자 ID가 올바르지 않습니다.") Long> userIds
) {

    public TeamAssignmentCommand toCommand() {
        return new TeamAssignmentCommand(
                teamId,
                userIds == null ? List.of() : userIds
        );
    }
}