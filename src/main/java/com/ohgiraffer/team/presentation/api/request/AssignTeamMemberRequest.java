package com.ohgiraffer.team.presentation.api.request;

import com.ohgiraffer.team.application.command.AssignTeamMemberCommand;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AssignTeamMemberRequest(
        @NotNull(message = "배정할 사용자 ID를 입력해주세요.")
        @Positive(message = "사용자 ID가 올바르지 않습니다.")
        Long userId
) {

    public AssignTeamMemberCommand toCommand(
            Long teamId,
            Long requesterId
    ) {
        return new AssignTeamMemberCommand(
                teamId,
                requesterId,
                userId
        );
    }
}