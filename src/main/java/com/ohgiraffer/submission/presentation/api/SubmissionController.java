package com.ohgiraffer.submission.presentation.api;

import com.ohgiraffer.security.user.CustomUserPrincipal;
import com.ohgiraffer.submission.application.usecase.CreateSubmissionResult;
import com.ohgiraffer.submission.application.usecase.CreateSubmissionUseCase;
import com.ohgiraffer.submission.presentation.api.request.CreateSubmissionRequest;
import com.ohgiraffer.submission.presentation.api.response.CreateSubmissionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/submission-boxes")
@RequiredArgsConstructor
public class SubmissionController {

    private final CreateSubmissionUseCase
            createSubmissionUseCase;

    @PostMapping(
            value = "/{submissionBoxId}/submissions",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<CreateSubmissionResponse>
    createSubmission(
            @PathVariable Long submissionBoxId,
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
                                submissionBoxId,
                                principal.getId()
                        ),
                        files == null
                                ? List.of()
                                : files
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        CreateSubmissionResponse.from(result)
                );
    }
}