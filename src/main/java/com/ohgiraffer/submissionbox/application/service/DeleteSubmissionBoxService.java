package com.ohgiraffer.submissionbox.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.submissionbox.application.usecase.DeleteSubmissionBoxUseCase;
import com.ohgiraffer.submissionbox.domain.repository.SubmissionBoxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ohgiraffer.submissionbox.domain.model.SubmissionBox;
import com.ohgiraffer.user.domain.model.Role;

@Service
@RequiredArgsConstructor
public class DeleteSubmissionBoxService
        implements DeleteSubmissionBoxUseCase {

    private final SubmissionBoxRepository submissionBoxRepository;

    @Override
    @Transactional
    public void delete(
            Long submissionBoxId,
            Long requesterId,
            Role requesterRole
    ) {
        validateSubmissionBoxId(submissionBoxId);

        SubmissionBox submissionBox =
                submissionBoxRepository
                        .findByIdForUpdate(submissionBoxId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.SUBMISSION_BOX_NOT_FOUND
                                )
                        );

        validateManagementAuthority(
                requesterId,
                requesterRole
        );

        if (submissionBoxRepository.hasSubmissions(
                submissionBoxId
        )) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_BOX_HAS_SUBMISSIONS
            );
        }

        submissionBoxRepository.deleteById(
                submissionBoxId
        );
    }

    private void validateManagementAuthority(
            Long requesterId,
            Role requesterRole
    ) {
        if (requesterId == null
                || requesterId <= 0
                || requesterRole == null) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_BOX_ACCESS_DENIED
            );
        }

        if (requesterRole != Role.MANAGER
                && requesterRole != Role.INSTRUCTOR) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_BOX_ACCESS_DENIED
            );
        }
    }

    private void validateSubmissionBoxId(
            Long submissionBoxId
    ) {
        if (submissionBoxId == null
                || submissionBoxId <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "제출함 ID가 올바르지 않습니다."
            );
        }
    }
}