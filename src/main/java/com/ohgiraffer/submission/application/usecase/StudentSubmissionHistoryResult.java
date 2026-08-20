package com.ohgiraffer.submission.application.usecase;

import java.util.List;

public record StudentSubmissionHistoryResult(

        Long studentId,

        String studentName,

        String email,

        int totalCount,

        int completedCount,

        List<StudentSubmissionHistoryItemResult> items
) {

    public StudentSubmissionHistoryResult {
        items = items == null
                ? List.of()
                : List.copyOf(items);
    }

    public static StudentSubmissionHistoryResult of(
            Long studentId,
            String studentName,
            String email,
            List<StudentSubmissionHistoryItemResult> items
    ) {
        List<StudentSubmissionHistoryItemResult> safeItems =
                items == null
                        ? List.of()
                        : List.copyOf(items);

        int completedCount =
                Math.toIntExact(
                        safeItems.stream()
                                .filter(
                                        StudentSubmissionHistoryItemResult
                                                ::completed
                                )
                                .count()
                );

        return new StudentSubmissionHistoryResult(
                studentId,
                studentName,
                email,
                safeItems.size(),
                completedCount,
                safeItems
        );
    }
}