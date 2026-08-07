package com.ohgiraffer.team.presentation.api.response;

import com.ohgiraffer.team.application.usecase.UnassignedStudentResult;

import java.util.List;

public record UnassignedStudentListResponse(
        List<UnassignedStudentResponse> students
) {

    public static UnassignedStudentListResponse from(
            List<UnassignedStudentResult> results
    ) {
        return new UnassignedStudentListResponse(
                results.stream()
                        .map(UnassignedStudentResponse::from)
                        .toList()
        );
    }
}