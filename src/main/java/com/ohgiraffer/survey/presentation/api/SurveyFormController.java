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
import com.ohgiraffer.survey.application.usecase.SurveySheetValidationResult;
import com.ohgiraffer.survey.application.usecase.ValidateSurveySheetUseCase;
import com.ohgiraffer.survey.presentation.api.request.ValidateSurveySheetRequest;
import com.ohgiraffer.survey.presentation.api.response.SurveySheetValidationResponse;
import com.ohgiraffer.survey.application.command.SaveSurveySheetLinkCommand;
import com.ohgiraffer.survey.application.usecase.SaveSurveySheetLinkResult;
import com.ohgiraffer.survey.application.usecase.SaveSurveySheetLinkUseCase;
import com.ohgiraffer.survey.presentation.api.request.SaveSurveySheetLinkRequest;
import com.ohgiraffer.survey.presentation.api.response.SaveSurveySheetLinkResponse;
import com.ohgiraffer.survey.application.usecase.GenerateSurveySummaryPdfUseCase;
import com.ohgiraffer.survey.application.usecase.SurveySummaryPdfResult;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;
import java.nio.charset.StandardCharsets;

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
    private final ValidateSurveySheetUseCase validateSurveySheetUseCase;
    private final SaveSurveySheetLinkUseCase saveSurveySheetLinkUseCase;
    private final GenerateSurveySummaryPdfUseCase generateSurveySummaryPdfUseCase;

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
    @PreAuthorize("hasAnyRole('MANAGER', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<List<SurveyFormListResponse>>
    getSurveyForms(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        List<SurveyFormListResult> results =
                getSurveyFormListUseCase.getSurveyForms(
                        principal.getId(),
                        principal.getUsername(),
                        principal.getRole()
                );

        List<SurveyFormListResponse> responses =
                results.stream()
                        .map(SurveyFormListResponse::from)
                        .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{surveyFormId}")
    @PreAuthorize(
            "hasAnyRole('MANAGER', 'INSTRUCTOR', 'STUDENT')"
    )
    public ResponseEntity<SurveyFormDetailResponse> getSurveyForm(
            @PathVariable Long surveyFormId,
            @AuthenticationPrincipal
            CustomUserPrincipal principal
    ) {
        SurveyFormDetailResult result =
                getSurveyFormDetailUseCase.getSurveyForm(
                        surveyFormId,
                        principal.getId(),
                        principal.getRole()
                );

        return ResponseEntity.ok(
                SurveyFormDetailResponse.from(
                        result
                )
        );
    }

    @PatchMapping("/{surveyFormId}")
    @PreAuthorize(
            "hasAnyRole('MANAGER', 'INSTRUCTOR')"
    )
    public ResponseEntity<UpdateSurveyFormResponse>
    updateSurveyForm(
            @PathVariable Long surveyFormId,
            @Valid
            @RequestBody
            UpdateSurveyFormRequest request,
            @AuthenticationPrincipal
            CustomUserPrincipal principal
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
                        command,
                        principal.getId(),
                        principal.getRole()
                );

        return ResponseEntity.ok(
                UpdateSurveyFormResponse.from(
                        result
                )
        );
    }

    @DeleteMapping("/{surveyFormId}")
    @PreAuthorize(
            "hasAnyRole('MANAGER', 'INSTRUCTOR')"
    )
    public ResponseEntity<Void> deleteSurveyForm(
            @PathVariable Long surveyFormId,
            @AuthenticationPrincipal
            CustomUserPrincipal principal
    ) {
        deleteSurveyFormUseCase.delete(
                surveyFormId,
                principal.getId(),
                principal.getRole()
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    @GetMapping("/{surveyFormId}/responses")
    @PreAuthorize(
            "hasAnyRole('MANAGER', 'INSTRUCTOR')"
    )
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
            int size,

            @AuthenticationPrincipal
            CustomUserPrincipal principal
    ) {
        SurveyResponseDetailResult result =
                getSurveyResponsesUseCase
                        .getSurveyResponses(
                                surveyFormId,
                                keyword,
                                responseStatus,
                                page,
                                size,
                                principal.getId(),
                                principal.getRole()
                        );

        return ResponseEntity.ok(
                SurveyResponseDetailResponse.from(
                        result
                )
        );
    }

    @PostMapping("/{surveyFormId}/sheet-link/validate")
    @PreAuthorize(
            "hasAnyRole('MANAGER', 'INSTRUCTOR')"
    )
    public ResponseEntity<SurveySheetValidationResponse>
    validateSurveySheet(
            @PathVariable Long surveyFormId,

            @Valid
            @RequestBody
            ValidateSurveySheetRequest request,

            @AuthenticationPrincipal
            CustomUserPrincipal principal
    ) {
        SurveySheetValidationResult result =
                validateSurveySheetUseCase.validate(
                        surveyFormId,
                        request.spreadsheetUrl(),
                        request.sheetName(),
                        principal.getId(),
                        principal.getRole()
                );

        return ResponseEntity.ok(
                SurveySheetValidationResponse.from(
                        result
                )
        );
    }

    @PutMapping("/{surveyFormId}/sheet-link")
    @PreAuthorize(
            "hasAnyRole('MANAGER', 'INSTRUCTOR')"
    )
    public ResponseEntity<SaveSurveySheetLinkResponse>
    saveSurveySheetLink(
            @PathVariable Long surveyFormId,

            @Valid
            @RequestBody
            SaveSurveySheetLinkRequest request,

            @AuthenticationPrincipal
            CustomUserPrincipal principal
    ) {
        SaveSurveySheetLinkCommand command =
                new SaveSurveySheetLinkCommand(
                        surveyFormId,
                        request.spreadsheetUrl(),
                        request.sheetName()

                );

        SaveSurveySheetLinkResult result =
                saveSurveySheetLinkUseCase.save(
                        command,
                        principal.getId(),
                        principal.getRole()
                );

        return ResponseEntity.ok(
                SaveSurveySheetLinkResponse.from(
                        result
                )
        );
    }

    @PostMapping(
            value = "/{surveyFormId}/summary",
            produces = MediaType.APPLICATION_PDF_VALUE
    )
    @PreAuthorize(
            "hasAnyRole('MANAGER', 'INSTRUCTOR')"
    )
    public ResponseEntity<byte[]>
    generateSurveySummaryPdf(
            @PathVariable Long surveyFormId,

            @AuthenticationPrincipal
            CustomUserPrincipal principal
    ) {
        SurveySummaryPdfResult result =
                generateSurveySummaryPdfUseCase
                        .generate(
                                surveyFormId,
                                principal.getId(),
                                principal.getRole()
                        );

        byte[] content =
                result.content();

        ContentDisposition contentDisposition =
                ContentDisposition.attachment()
                        .filename(
                                result.fileName(),
                                StandardCharsets.UTF_8
                        )
                        .build();

        return ResponseEntity.ok()
                .contentType(
                        MediaType.APPLICATION_PDF
                )
                .contentLength(
                        content.length
                )
                .cacheControl(
                        CacheControl.noStore()
                )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        contentDisposition.toString()
                )
                .body(content);
    }

}