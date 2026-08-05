package com.ohgiraffer.submissionbox.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.submissionbox.application.usecase.DeleteSubmissionBoxUseCase;
import com.ohgiraffer.submissionbox.domain.repository.SubmissionBoxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteSubmissionBoxService
        implements DeleteSubmissionBoxUseCase {

    private final SubmissionBoxRepository submissionBoxRepository;

    @Override
    @Transactional
    public void delete(
            Long submissionBoxId
    ) {
        validateSubmissionBoxId(submissionBoxId);

        if (!submissionBoxRepository.existsById(
                submissionBoxId
        )) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_BOX_NOT_FOUND
            );
        }

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