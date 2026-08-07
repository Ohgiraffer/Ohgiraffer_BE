package com.ohgiraffer.submission.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.global.s3.S3UrlResolver;
import com.ohgiraffer.submission.application.usecase.DownloadSubmissionFileResult;
import com.ohgiraffer.submission.application.usecase.DownloadSubmissionFileUseCase;
import com.ohgiraffer.submission.domain.model.SubmissionItemValue;
import com.ohgiraffer.submission.domain.repository.SubmissionItemValueRepository;
import com.ohgiraffer.user.domain.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DownloadSubmissionFileService
        implements DownloadSubmissionFileUseCase {

    private final SubmissionItemValueRepository
            submissionItemValueRepository;

    private final S3UrlResolver
            s3UrlResolver;

    @Override
    public DownloadSubmissionFileResult createDownload(
            Long submissionItemValueId,
            Long requesterId,
            Role requesterRole
    ) {
        validateRequest(
                submissionItemValueId,
                requesterId,
                requesterRole
        );

        SubmissionItemValue value =
                submissionItemValueRepository
                        .findById(
                                submissionItemValueId
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode
                                                .SUBMISSION_FILE_NOT_FOUND
                                )
                        );

        validateFileValue(value);

        String downloadUrl =
                s3UrlResolver.resolveDownload(
                        value.getFileKey(),
                        value.getOriginalFileName()
                );

        return new DownloadSubmissionFileResult(
                value.getId(),
                value.getOriginalFileName(),
                resolveContentType(
                        value.getContentType()
                ),
                value.getFileSize(),
                downloadUrl
        );
    }

    private void validateRequest(
            Long submissionItemValueId,
            Long requesterId,
            Role requesterRole
    ) {
        if (submissionItemValueId == null
                || submissionItemValueId <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "제출 파일 값 ID가 올바르지 않습니다."
            );
        }

        if (requesterId == null
                || requesterId <= 0
                || requesterRole == null) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_ACCESS_DENIED
            );
        }

        boolean allowedRole =
                requesterRole == Role.STUDENT
                        || requesterRole == Role.MANAGER
                        || requesterRole
                        == Role.INSTRUCTOR;

        if (!allowedRole) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_ACCESS_DENIED
            );
        }
    }

    private void validateFileValue(
            SubmissionItemValue value
    ) {
        boolean file =
                value.getFileKey() != null
                        && !value.getFileKey().isBlank();

        boolean validOriginalFileName =
                value.getOriginalFileName() != null
                        && !value.getOriginalFileName()
                        .isBlank();

        if (!file || !validOriginalFileName) {
            throw new BusinessException(
                    ErrorCode
                            .SUBMISSION_FILE_DOWNLOAD_NOT_ALLOWED
            );
        }
    }

    private String resolveContentType(
            String contentType
    ) {
        if (contentType == null
                || contentType.isBlank()) {
            return "application/octet-stream";
        }

        return contentType;
    }
}