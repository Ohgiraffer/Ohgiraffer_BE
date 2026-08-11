package com.ohgiraffer.team.presentation.api.response;

import com.ohgiraffer.team.application.usecase.UnassignedStudentResult;

public record UnassignedStudentResponse(
        Long userId,
        String name,
        String email,
        String profileImgUrl
) {

    public static UnassignedStudentResponse from(
            UnassignedStudentResult result
    ) {
        return new UnassignedStudentResponse(
                result.userId(),
                result.name(),
                result.email(),
                result.profileImgUrl()
        );
    }
}