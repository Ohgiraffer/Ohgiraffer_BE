package com.ohgiraffer.submission.presentation.api.response;

import com.ohgiraffer.submission.application.usecase.StudentSubmissionHistoryResult;

import java.util.List;

public record StudentSubmissionHistoryResponse(

        Long studentId,

        String studentName,

        String email,

        int totalCount,

        int completedCount,

        List<StudentSubmissionHistoryItemResponse> items
) {

    public StudentSubmissionHistoryResponse {
        items = items == null
                ? List.of()
                : List.copyOf(items);
    }

    public static StudentSubmissionHistoryResponse from(
            StudentSubmissionHistoryResult result
    ) {
        return new StudentSubmissionHistoryResponse(
                result.studentId(),
                result.studentName(),
                result.email(),
                result.totalCount(),
                result.completedCount(),
                result.items()
                        .stream()
                        .map(
                                StudentSubmissionHistoryItemResponse
                                        ::from
                        )
                        .toList()
        );
    }
}