package com.ohgiraffer.submissionbox.application.usecase;

import com.ohgiraffer.submissionbox.domain.model.LatePolicy;
import com.ohgiraffer.submissionbox.domain.model.SubmissionBox;
import com.ohgiraffer.submissionbox.domain.model.SubmissionTargetScope;

import java.time.LocalDateTime;

public record SubmissionBoxListResult(
        Long submissionBoxId,
        String projectName,
        SubmissionTargetScope targetScope,
        LocalDateTime startAt,
        LocalDateTime dueAt,
        LatePolicy latePolicy,
        int itemCount,
        SubmissionBoxStatus status,
        boolean acceptingSubmissions,
        boolean lateSubmission,
        Integer submittedCount,
        Integer targetCount,
        Boolean submitted,
        Long submissionId
) {

    /**
     * 훈련생용 목록 결과.
     *
     * 훈련생은 본인 또는 소속 팀의 제출 여부와 submissionId를 받는다.
     * 전체 제출 현황은 노출하지 않는다.
     */
    public static SubmissionBoxListResult forStudent(
            SubmissionBox submissionBox,
            LocalDateTime now,
            Long submissionId
    ) {
        SubmissionBoxStatus status =
                calculateStatus(submissionBox, now);

        return new SubmissionBoxListResult(
                submissionBox.getId(),
                submissionBox.getProjectName(),
                submissionBox.getTargetScope(),
                submissionBox.getStartAt(),
                submissionBox.getDueAt(),
                submissionBox.getLatePolicy(),
                submissionBox.getItems().size(),
                status,
                canSubmit(submissionBox, now),
                now.isAfter(submissionBox.getDueAt()),
                null,
                null,
                submissionId != null,
                submissionId
        );
    }

    /**
     * 매니저·강사용 목록 결과.
     *
     * 운영진은 제출 대상 수와 실제 제출 수를 받는다.
     * 특정 훈련생의 제출 여부는 반환하지 않는다.
     */
    public static SubmissionBoxListResult forStaff(
            SubmissionBox submissionBox,
            LocalDateTime now,
            int submittedCount,
            int targetCount
    ) {
        SubmissionBoxStatus status =
                calculateStatus(submissionBox, now);

        return new SubmissionBoxListResult(
                submissionBox.getId(),
                submissionBox.getProjectName(),
                submissionBox.getTargetScope(),
                submissionBox.getStartAt(),
                submissionBox.getDueAt(),
                submissionBox.getLatePolicy(),
                submissionBox.getItems().size(),
                status,
                canSubmit(submissionBox, now),
                now.isAfter(submissionBox.getDueAt()),
                submittedCount,
                targetCount,
                null,
                null
        );
    }

    private static SubmissionBoxStatus calculateStatus(
            SubmissionBox submissionBox,
            LocalDateTime now
    ) {
        if (now.isBefore(submissionBox.getStartAt())) {
            return SubmissionBoxStatus.UPCOMING;
        }

        if (now.isAfter(submissionBox.getDueAt())) {
            return SubmissionBoxStatus.CLOSED;
        }

        return SubmissionBoxStatus.OPEN;
    }

    private static boolean canSubmit(
            SubmissionBox submissionBox,
            LocalDateTime now
    ) {
        if (now.isBefore(submissionBox.getStartAt())) {
            return false;
        }

        if (!now.isAfter(submissionBox.getDueAt())) {
            return true;
        }

        return submissionBox.getLatePolicy()
                == LatePolicy.ALLOW;
    }
}