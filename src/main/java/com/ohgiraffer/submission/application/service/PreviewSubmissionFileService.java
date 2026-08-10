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
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PreviewSubmissionFileService
        implements PreviewSubmissionFileUseCase {

    private static final Map<String, Set<String>>
            PREVIEWABLE_FILE_TYPES =
            Map.ofEntries(
                    Map.entry(
                            "pdf",
                            Set.of(
                                    "application/pdf"
                            )
                    ),
                    Map.entry(
                            "mp4",
                            Set.of(
                                    "video/mp4"
                            )
                    ),
                    Map.entry(
                            "mov",
                            Set.of(
                                    "video/quicktime"
                            )
                    ),
                    Map.entry(
                            "jpg",
                            Set.of(
                                    "image/jpeg"
                            )
                    ),
                    Map.entry(
                            "jpeg",
                            Set.of(
                                    "image/jpeg"
                            )
                    ),
                    Map.entry(
                            "png",
                            Set.of(
                                    "image/png"
                            )
                    ),
                    Map.entry(
                            "webp",
                            Set.of(
                                    "image/webp"
                            )
                    ),
                    Map.entry(
                            "gif",
                            Set.of(
                                    "image/gif"
                            )
                    )
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

        validatePreviewableFile(
                value.getOriginalFileName(),
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
            throwPreviewNotSupported();
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

        normalized = normalized
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );

        if (normalized.isBlank()) {
            throwPreviewNotSupported();
        }

        return normalized;
    }

    private void validatePreviewableFile(
            String originalFileName,
            String contentType
    ) {
        String extension =
                extractExtension(
                        originalFileName
                );

        Set<String> allowedContentTypes =
                PREVIEWABLE_FILE_TYPES.get(
                        extension
                );

        if (allowedContentTypes == null
                || !allowedContentTypes.contains(
                contentType
        )) {
            throwPreviewNotSupported();
        }
    }

    private String extractExtension(
            String originalFileName
    ) {
        if (originalFileName == null
                || originalFileName.isBlank()) {
            throwPreviewNotSupported();
        }

        String normalizedFileName =
                originalFileName.trim();

        int extensionIndex =
                normalizedFileName.lastIndexOf('.');

        if (extensionIndex < 0
                || extensionIndex
                == normalizedFileName.length() - 1) {
            throwPreviewNotSupported();
        }

        String extension =
                normalizedFileName
                        .substring(
                                extensionIndex + 1
                        )
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        if (extension.isBlank()) {
            throwPreviewNotSupported();
        }

        return extension;
    }

    private void throwPreviewNotSupported() {
        throw new BusinessException(
                ErrorCode
                        .SUBMISSION_FILE_PREVIEW_NOT_SUPPORTED
        );
    }
}