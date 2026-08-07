package com.ohgiraffer.submission.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.global.s3.S3UrlResolver;
import com.ohgiraffer.submission.application.usecase.PreviewSubmissionFileResult;
import com.ohgiraffer.submission.application.usecase.PreviewSubmissionFileUseCase;
import com.ohgiraffer.submission.domain.model.SubmissionItemValue;
import com.ohgiraffer.user.domain.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PreviewSubmissionFileService
        implements PreviewSubmissionFileUseCase {

    private static final Set<String>
            PREVIEWABLE_CONTENT_TYPES =
            Set.of(
                    "application/pdf",
                    "video/mp4",
                    "video/quicktime",
                    "image/jpeg",
                    "image/png",
                    "image/webp",
                    "image/gif"
            );

    private final SubmissionFileAccessService
            submissionFileAccessService;

    private final S3UrlResolver
            s3UrlResolver;

    @Override
    public PreviewSubmissionFileResult createPreview(
            Long submissionItemValueId,
            Long requesterId,
            Role requesterRole
    ) {
        SubmissionItemValue value =
                submissionFileAccessService
                        .getAccessibleFile(
                                submissionItemValueId,
                                requesterId,
                                requesterRole
                        );

        String contentType =
                normalizeContentType(
                        value.getContentType()
                );

        validatePreviewableContentType(
                contentType
        );

        String previewUrl =
                s3UrlResolver.resolvePreview(
                        value.getFileKey(),
                        value.getOriginalFileName(),
                        contentType
                );

        return new PreviewSubmissionFileResult(
                value.getId(),
                value.getOriginalFileName(),
                contentType,
                value.getFileSize(),
                previewUrl
        );
    }

    private String normalizeContentType(
            String contentType
    ) {
        if (contentType == null
                || contentType.isBlank()) {
            throw new BusinessException(
                    ErrorCode
                            .SUBMISSION_FILE_PREVIEW_NOT_SUPPORTED
            );
        }

        int parameterIndex =
                contentType.indexOf(';');

        String normalized =
                parameterIndex >= 0
                        ? contentType.substring(
                        0,
                        parameterIndex
                )
                        : contentType;

        return normalized
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }

    private void validatePreviewableContentType(
            String contentType
    ) {
        if (!PREVIEWABLE_CONTENT_TYPES.contains(
                contentType
        )) {
            throw new BusinessException(
                    ErrorCode
                            .SUBMISSION_FILE_PREVIEW_NOT_SUPPORTED
            );
        }
    }
}