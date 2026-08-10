package com.ohgiraffer.team.presentation.api.request;

import com.ohgiraffer.team.application.command.TeamConfigurationCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TeamConfigurationRequest(
        @Positive(message = "팀 ID가 올바르지 않습니다.")
        Long teamId,

        @NotBlank(message = "팀명을 입력해주세요.")
        @Size(max = 100, message = "팀명은 100자를 초과할 수 없습니다.")
        String name,

        List<@Positive(message = "사용자 ID가 올바르지 않습니다.") Long> userIds
) {

    public TeamConfigurationCommand toCommand() {
        return new TeamConfigurationCommand(
                teamId,
                name,
                userIds
        );
    }
}