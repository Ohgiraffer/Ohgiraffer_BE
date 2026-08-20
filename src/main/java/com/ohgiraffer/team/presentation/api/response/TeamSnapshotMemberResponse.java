package com.ohgiraffer.team.presentation.api.response;

import com.ohgiraffer.team.application.usecase.TeamSnapshotMemberResult;

public record TeamSnapshotMemberResponse(
        Long userId,
        String userName,
        String profileImgUrl
) {

    public static TeamSnapshotMemberResponse from(
            TeamSnapshotMemberResult result
    ) {
        return new TeamSnapshotMemberResponse(
                result.userId(),
                result.userName(),
                result.profileImgUrl()
        );
    }
}