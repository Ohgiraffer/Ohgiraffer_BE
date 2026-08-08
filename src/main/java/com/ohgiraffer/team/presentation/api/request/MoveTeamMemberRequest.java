package com.ohgiraffer.team.presentation.api.request;

import com.ohgiraffer.team.application.command.MoveTeamMemberCommand;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MoveTeamMemberRequest(
        @NotNull(message = "이동할 팀 ID를 입력해주세요.")
        @Positive(message = "이동할 팀 ID가 올바르지 않습니다.")
        Long targetTeamId
) {

    public MoveTeamMemberCommand toCommand(
            Long sourceTeamId,
            Long teamMemberId,
            Long requesterId
    ) {
        return new MoveTeamMemberCommand(
                sourceTeamId,
                targetTeamId,
                teamMemberId,
                requesterId
        );
    }
}