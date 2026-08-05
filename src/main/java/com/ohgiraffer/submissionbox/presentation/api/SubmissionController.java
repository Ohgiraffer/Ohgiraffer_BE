package com.ohgiraffer.submissionbox.presentation.api;

import com.ohgiraffer.security.user.CustomUserPrincipal;
import com.ohgiraffer.submissionbox.application.command.CreateSubmissionBoxCommand;
import com.ohgiraffer.submissionbox.application.usecase.CreateSubmissionBoxResult;
import com.ohgiraffer.submissionbox.application.usecase.CreateSubmissionBoxUseCase;
import com.ohgiraffer.submissionbox.application.usecase.GetSubmissionBoxDetailUseCase;
import com.ohgiraffer.submissionbox.application.usecase.GetSubmissionBoxListUseCase;
import com.ohgiraffer.submissionbox.application.usecase.SubmissionBoxDetailResult;
import com.ohgiraffer.submissionbox.application.usecase.SubmissionBoxListResult;
import com.ohgiraffer.submissionbox.presentation.api.request.CreateSubmissionBoxRequest;
import com.ohgiraffer.submissionbox.presentation.api.response.CreateSubmissionBoxResponse;
import com.ohgiraffer.submissionbox.presentation.api.response.SubmissionBoxDetailResponse;
import com.ohgiraffer.submissionbox.presentation.api.response.SubmissionBoxListResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/submission-boxes")
@RequiredArgsConstructor
public class SubmissionController {

    private final CreateSubmissionBoxUseCase createSubmissionBoxUseCase;
    private final GetSubmissionBoxListUseCase getSubmissionBoxListUseCase;
    private final GetSubmissionBoxDetailUseCase getSubmissionBoxDetailUseCase;

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'INSTRUCTOR')")
    public ResponseEntity<CreateSubmissionBoxResponse> createSubmissionBox(
            @Valid @RequestBody CreateSubmissionBoxRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        CreateSubmissionBoxCommand command =
                request.toCommand(principal.getId());

        CreateSubmissionBoxResult result =
                createSubmissionBoxUseCase.create(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CreateSubmissionBoxResponse.from(result));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<List<SubmissionBoxListResponse>>
    getSubmissionBoxes() {

        List<SubmissionBoxListResult> results =
                getSubmissionBoxListUseCase
                        .getSubmissionBoxes();

        List<SubmissionBoxListResponse> responses =
                results.stream()
                        .map(SubmissionBoxListResponse::from)
                        .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{submissionBoxId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<SubmissionBoxDetailResponse>
    getSubmissionBox(
            @PathVariable Long submissionBoxId
    ) {
        SubmissionBoxDetailResult result =
                getSubmissionBoxDetailUseCase
                        .getSubmissionBox(submissionBoxId);

        return ResponseEntity.ok(
                SubmissionBoxDetailResponse.from(result)
        );
    }
}