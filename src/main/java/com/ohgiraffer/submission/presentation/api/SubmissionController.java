package com.ohgiraffer.submission.presentation.api;

import com.ohgiraffer.security.user.CustomUserPrincipal;
import com.ohgiraffer.submission.application.usecase.CreateSubmissionResult;
import com.ohgiraffer.submission.application.usecase.CreateSubmissionUseCase;
import com.ohgiraffer.submission.application.usecase.UpdateSubmissionResult;
import com.ohgiraffer.submission.application.usecase.UpdateSubmissionUseCase;
import com.ohgiraffer.submission.presentation.api.request.CreateSubmissionRequest;
import com.ohgiraffer.submission.presentation.api.request.UpdateSubmissionRequest;
import com.ohgiraffer.submission.presentation.api.response.CreateSubmissionResponse;
import com.ohgiraffer.submission.presentation.api.response.UpdateSubmissionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final CreateSubmissionUseCase
            createSubmissionUseCase;

    private final UpdateSubmissionUseCase
            updateSubmissionUseCase;

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<CreateSubmissionResponse>
    createSubmission(
            @Valid
            @RequestPart("request")
            CreateSubmissionRequest request,
            @RequestPart(
                    value = "files",
                    required = false
            )
            List<MultipartFile> files,
            @AuthenticationPrincipal
            CustomUserPrincipal principal
    ) {
        CreateSubmissionResult result =
                createSubmissionUseCase.create(
                        request.toCommand(
                                principal.getId()
                        ),
                        files == null
                                ? List.of()
                                : files
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        CreateSubmissionResponse.from(
                                result
                        )
                );
    }

    @PatchMapping(
            value = "/{submissionId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<UpdateSubmissionResponse>
    updateSubmission(
            @PathVariable Long submissionId,
            @Valid
            @RequestPart("request")
            UpdateSubmissionRequest request,
            @RequestPart(
                    value = "files",
                    required = false
            )
            List<MultipartFile> files,
            @AuthenticationPrincipal
            CustomUserPrincipal principal
    ) {
        UpdateSubmissionResult result =
                updateSubmissionUseCase.update(
                        request.toCommand(
                                submissionId,
                                principal.getId()
                        ),
                        files == null
                                ? List.of()
                                : files
                );

        return ResponseEntity.ok(
                UpdateSubmissionResponse.from(
                        result
                )
        );
    }
}