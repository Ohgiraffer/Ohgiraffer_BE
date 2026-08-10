package com.ohgiraffer.submission.application.service;

import com.ohgiraffer.global.s3.S3UrlResolver;
import com.ohgiraffer.submission.application.usecase.DownloadSubmissionFileResult;
import com.ohgiraffer.submission.application.usecase.DownloadSubmissionFileUseCase;
import com.ohgiraffer.submission.domain.model.SubmissionItemValue;
import com.ohgiraffer.user.domain.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DownloadSubmissionFileService
        implements DownloadSubmissionFileUseCase {

    private final SubmissionFileAccessService
            submissionFileAccessService;

    private final S3UrlResolver
            s3UrlResolver;

    @Override
    public DownloadSubmissionFileResult createDownload(
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
                resolveContentType(
                        value.getContentType()
                );

        String downloadUrl =
                s3UrlResolver.resolveDownload(
                        value.getFileKey(),
                        value.getOriginalFileName()
                );

        return new DownloadSubmissionFileResult(
                value.getId(),
                value.getOriginalFileName(),
                contentType,
                value.getFileSize(),
                downloadUrl
        );
    }

    private String resolveContentType(
            String contentType
    ) {
        if (contentType == null
                || contentType.isBlank()) {
            return "application/octet-stream";
        }

        return contentType.trim();
    }
}