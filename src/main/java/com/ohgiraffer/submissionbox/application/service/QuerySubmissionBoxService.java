package com.ohgiraffer.submissionbox.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.submissionbox.application.usecase.GetSubmissionBoxDetailUseCase;
import com.ohgiraffer.submissionbox.application.usecase.GetSubmissionBoxListUseCase;
import com.ohgiraffer.submissionbox.application.usecase.SubmissionBoxDetailResult;
import com.ohgiraffer.submissionbox.application.usecase.SubmissionBoxListResult;
import com.ohgiraffer.submissionbox.domain.model.SubmissionBox;
import com.ohgiraffer.submissionbox.domain.repository.SubmissionBoxRepository;
import com.ohgiraffer.submission.domain.model.Submission;
import com.ohgiraffer.submission.domain.repository.StudentTeamRepository;
import com.ohgiraffer.submission.domain.repository.SubmissionRepository;
import com.ohgiraffer.submissionbox.domain.model.SubmissionTargetScope;
import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.submissionbox.application.usecase.SubmissionStatusResult;

import java.util.ArrayList;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuerySubmissionBoxService
        implements GetSubmissionBoxListUseCase,
        GetSubmissionBoxDetailUseCase {

    private final SubmissionBoxRepository submissionBoxRepository;
    private final SubmissionRepository submissionRepository;
    private final StudentTeamRepository studentTeamRepository;

    @Override
    public List<SubmissionBoxListResult> getSubmissionBoxes(
            Long userId,
            Role role
    ) {
        LocalDateTime now = LocalDateTime.now();

        return submissionBoxRepository.findAll()
                .stream()
                .map(submissionBox -> {
                    if (role != Role.STUDENT) {
                        return SubmissionBoxListResult.from(
                                submissionBox,
                                now
                        );
                    }

                    Long submissionId =
                            findStudentSubmissionId(
                                    submissionBox,
                                    userId
                            );

                    return SubmissionBoxListResult.from(
                            submissionBox,
                            now,
                            submissionId
                    );
                })
                .toList();
    }

    private Long findStudentSubmissionId(
            SubmissionBox submissionBox,
            Long studentId
    ) {
        if (submissionBox.getTargetScope()
                == SubmissionTargetScope.INDIVIDUAL) {

            return submissionRepository
                    .findBySubmissionBoxIdAndOwnerUserId(
                            submissionBox.getId(),
                            studentId
                    )
                    .map(Submission::getId)
                    .orElse(null);
        }

        Optional<Long> teamId =
                studentTeamRepository
                        .findActiveTeamIdByStudentId(
                                studentId
                        );

        if (teamId.isEmpty()) {
            return null;
        }

        return submissionRepository
                .findBySubmissionBoxIdAndTeamId(
                        submissionBox.getId(),
                        teamId.get()
                )
                .map(Submission::getId)
                .orElse(null);
    }

    @Override
    public SubmissionBoxDetailResult getSubmissionBox(
            Long submissionBoxId,
            Long requesterId,
            Role requesterRole
    ) {
        validateDetailRequest(
                submissionBoxId,
                requesterId,
                requesterRole
        );

        SubmissionBox submissionBox =
                submissionBoxRepository
                        .findById(submissionBoxId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.SUBMISSION_BOX_NOT_FOUND
                                )
                        );

        LocalDateTime now =
                LocalDateTime.now();

        SubmissionBoxDetailResult basicDetail =
                SubmissionBoxDetailResult.from(
                        submissionBox,
                        now
                );

        boolean editable =
                isEditable(
                        submissionBox,
                        now
                );

        Optional<Long> requesterTeamId =
                findRequesterTeamId(
                        submissionBox,
                        requesterId
                );

        boolean eligibleToSubmit =
                isEligibleToSubmit(
                        submissionBox,
                        requesterTeamId
                );

        List<Submission> submissions =
                submissionRepository
                        .findAllBySubmissionBoxId(
                                submissionBoxId
                        );

        List<SubmissionStatusResult> submissionStatuses =
                new ArrayList<>();

        Long mySubmissionId = null;

        for (Submission submission : submissions) {
            boolean mine =
                    isRequesterSubmission(
                            submissionBox,
                            submission,
                            requesterId,
                            requesterTeamId
                    );

            if (mine) {
                mySubmissionId =
                        submission.getId();
            }

            submissionStatuses.add(
                    SubmissionStatusResult.submitted(
                            submission,
                            createTemporaryTargetName(
                                    submissionBox,
                                    submission
                            ),
                            mine,
                            editable
                    )
            );
        }

        if (eligibleToSubmit
                && mySubmissionId == null) {
            submissionStatuses.add(
                    createMyNotSubmittedStatus(
                            submissionBox,
                            requesterId,
                            requesterTeamId,
                            basicDetail.acceptingSubmissions()
                    )
            );
        }

        return SubmissionBoxDetailResult.from(
                submissionBox,
                now,
                null,
                mySubmissionId,
                eligibleToSubmit,
                submissionStatuses
        );
    }

    private void validateDetailRequest(
            Long submissionBoxId,
            Long requesterId,
            Role requesterRole
    ) {
        if (submissionBoxId == null
                || submissionBoxId <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "제출함 ID가 올바르지 않습니다."
            );
        }

        if (requesterId == null
                || requesterId <= 0) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_BOX_ACCESS_DENIED
            );
        }

        if (requesterRole != Role.STUDENT) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_BOX_ACCESS_DENIED
            );
        }
    }

    private Optional<Long> findRequesterTeamId(
            SubmissionBox submissionBox,
            Long requesterId
    ) {
        if (submissionBox.getTargetScope()
                != SubmissionTargetScope.TEAM) {
            return Optional.empty();
        }

        return studentTeamRepository
                .findActiveTeamIdByStudentId(
                        requesterId
                );
    }

    private boolean isEligibleToSubmit(
            SubmissionBox submissionBox,
            Optional<Long> requesterTeamId
    ) {
        if (submissionBox.getTargetScope()
                == SubmissionTargetScope.INDIVIDUAL) {
            return true;
        }

        return requesterTeamId.isPresent();
    }

    private boolean isRequesterSubmission(
            SubmissionBox submissionBox,
            Submission submission,
            Long requesterId,
            Optional<Long> requesterTeamId
    ) {
        if (submissionBox.getTargetScope()
                == SubmissionTargetScope.INDIVIDUAL) {
            return requesterId.equals(
                    submission.getOwnerUserId()
            );
        }

        return requesterTeamId
                .map(teamId ->
                        teamId.equals(
                                submission.getTeamId()
                        )
                )
                .orElse(false);
    }

    private String createTemporaryTargetName(
            SubmissionBox submissionBox,
            Submission submission
    ) {
        if (submissionBox.getTargetScope()
                == SubmissionTargetScope.TEAM) {
            return "팀 " + submission.getTeamId();
        }

        return "훈련생 " + submission.getOwnerUserId();
    }

    private SubmissionStatusResult createMyNotSubmittedStatus(
            SubmissionBox submissionBox,
            Long requesterId,
            Optional<Long> requesterTeamId,
            boolean acceptingSubmissions
    ) {
        if (submissionBox.getTargetScope()
                == SubmissionTargetScope.INDIVIDUAL) {
            return SubmissionStatusResult.notSubmitted(
                    requesterId,
                    "훈련생 " + requesterId,
                    true,
                    acceptingSubmissions
            );
        }

        Long teamId =
                requesterTeamId.orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.SUBMISSION_ACCESS_DENIED
                        )
                );

        return SubmissionStatusResult.notSubmitted(
                teamId,
                "팀 " + teamId,
                true,
                acceptingSubmissions
        );
    }

    private boolean isEditable(
            SubmissionBox submissionBox,
            LocalDateTime now
    ) {
        if (now.isBefore(
                submissionBox.getStartAt()
        )) {
            return false;
        }

        return !now.isAfter(
                submissionBox.getDueAt()
        );
    }

}