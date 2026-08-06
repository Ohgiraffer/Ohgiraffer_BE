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
            Long submissionBoxId
    ) {
        if (submissionBoxId == null
                || submissionBoxId <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "제출함 ID가 올바르지 않습니다."
            );
        }

        SubmissionBox submissionBox =
                submissionBoxRepository
                        .findById(submissionBoxId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.SUBMISSION_BOX_NOT_FOUND
                                )
                        );

        return SubmissionBoxDetailResult.from(
                submissionBox,
                LocalDateTime.now()
        );
    }
}