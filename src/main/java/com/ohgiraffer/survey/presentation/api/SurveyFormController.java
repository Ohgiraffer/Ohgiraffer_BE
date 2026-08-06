package com.ohgiraffer.survey.presentation.api;

import com.ohgiraffer.security.user.CustomUserPrincipal;
import com.ohgiraffer.survey.application.command.CreateSurveyFormCommand;
import com.ohgiraffer.survey.application.usecase.CreateSurveyFormResult;
import com.ohgiraffer.survey.application.usecase.CreateSurveyFormUseCase;
import com.ohgiraffer.survey.application.usecase.GetSurveyFormDetailUseCase;
import com.ohgiraffer.survey.application.usecase.GetSurveyFormListUseCase;
import com.ohgiraffer.survey.application.usecase.SurveyFormDetailResult;
import com.ohgiraffer.survey.application.usecase.SurveyFormListResult;
import com.ohgiraffer.survey.presentation.api.request.CreateSurveyFormRequest;
import com.ohgiraffer.survey.presentation.api.response.CreateSurveyFormResponse;
import com.ohgiraffer.survey.presentation.api.response.SurveyFormDetailResponse;
import com.ohgiraffer.survey.presentation.api.response.SurveyFormListResponse;
import com.ohgiraffer.survey.application.command.UpdateSurveyFormCommand;
import com.ohgiraffer.survey.application.usecase.UpdateSurveyFormResult;
import com.ohgiraffer.survey.application.usecase.UpdateSurveyFormUseCase;
import com.ohgiraffer.survey.presentation.api.request.UpdateSurveyFormRequest;
import com.ohgiraffer.survey.presentation.api.response.UpdateSurveyFormResponse;
import com.ohgiraffer.survey.application.usecase.DeleteSurveyFormUseCase;
import com.ohgiraffer.survey.application.usecase.GetSurveyResponsesUseCase;
import com.ohgiraffer.survey.application.usecase.SurveyResponseDetailResult;
import com.ohgiraffer.survey.application.usecase.SurveyResponseStatus;
import com.ohgiraffer.survey.presentation.api.response.SurveyResponseDetailResponse;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/survey-forms")
@RequiredArgsConstructor
public class SurveyFormController {

    private final CreateSurveyFormUseCase createSurveyFormUseCase;
    private final GetSurveyFormListUseCase getSurveyFormListUseCase;
    private final GetSurveyFormDetailUseCase getSurveyFormDetailUseCase;
    private final UpdateSurveyFormUseCase updateSurveyFormUseCase;
    private final DeleteSurveyFormUseCase deleteSurveyFormUseCase;
    private final GetSurveyResponsesUseCase getSurveyResponsesUseCase;

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'INSTRUCTOR')")
    public ResponseEntity<CreateSurveyFormResponse> createSurveyForm(
            @Valid @RequestBody CreateSurveyFormRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        CreateSurveyFormCommand command =
                new CreateSurveyFormCommand(
                        request.title(),
                        request.dueAt(),
                        principal.getId()
                );

        CreateSurveyFormResult result =
                createSurveyFormUseCase.create(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CreateSurveyFormResponse.from(result));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'INSTRUCTOR')")
    public ResponseEntity<List<SurveyFormListResponse>> getSurveyForms() {

        List<SurveyFormListResult> results =
                getSurveyFormListUseCase.getSurveyForms();

        List<SurveyFormListResponse> responses =
                results.stream()
                        .map(SurveyFormListResponse::from)
                        .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{surveyFormId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'INSTRUCTOR')")
    public ResponseEntity<SurveyFormDetailResponse> getSurveyForm(
            @PathVariable Long surveyFormId
    ) {
        SurveyFormDetailResult result =
                getSurveyFormDetailUseCase.getSurveyForm(
                        surveyFormId
                );

        return ResponseEntity.ok(
                SurveyFormDetailResponse.from(result)
        );
    }

    @PatchMapping("/{surveyFormId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'INSTRUCTOR')")
    public ResponseEntity<UpdateSurveyFormResponse> updateSurveyForm(
            @PathVariable Long surveyFormId,
            @Valid @RequestBody UpdateSurveyFormRequest request
    ) {
        UpdateSurveyFormCommand command =
                new UpdateSurveyFormCommand(
                        surveyFormId,
                        request.title(),
                        request.dueAt(),
                        request.status()
                );

        UpdateSurveyFormResult result =
                updateSurveyFormUseCase.update(
                        command
                );

        return ResponseEntity.ok(
                UpdateSurveyFormResponse.from(result)
        );
    }

    @DeleteMapping("/{surveyFormId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'INSTRUCTOR')")
    public ResponseEntity<Void> deleteSurveyForm(
            @PathVariable Long surveyFormId
    ) {
        deleteSurveyFormUseCase.delete(surveyFormId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{surveyFormId}/responses")
    @PreAuthorize("hasAnyRole('MANAGER', 'INSTRUCTOR')")
    public ResponseEntity<SurveyResponseDetailResponse>
    getSurveyResponses(
            @PathVariable Long surveyFormId,
            @RequestParam(
                    required = false,
                    defaultValue = ""
            )
            String keyword,
            @RequestParam(
                    required = false,
                    defaultValue = "ALL"
            )
            SurveyResponseStatus responseStatus,
            @RequestParam(
                    required = false,
                    defaultValue = "0"
            )
            int page,
            @RequestParam(
                    required = false,
                    defaultValue = "20"
            )
            int size
    ) {
        SurveyResponseDetailResult result =
                getSurveyResponsesUseCase
                        .getSurveyResponses(
                                surveyFormId,
                                keyword,
                                responseStatus,
                                page,
                                size
                        );

        return ResponseEntity.ok(
                SurveyResponseDetailResponse.from(
                        result
                )
        );
    }

}