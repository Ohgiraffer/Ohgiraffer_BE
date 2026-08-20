package com.ohgiraffer.team.application.usecase;

public record TeamSnapshotMemberResult(
        Long userId,
        String userName,
        String profileImgUrl
) {
}