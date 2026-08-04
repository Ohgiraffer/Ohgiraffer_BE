//package com.ohgiraffer.survey.presentation.api;
//
//import com.ohgiraffer.security.user.CustomUserPrincipal;
//import com.ohgiraffer.survey.application.command.CreateSurveyFormCommand;
//import com.ohgiraffer.survey.application.usecase.CreateSurveyFormResult;
//import com.ohgiraffer.survey.application.usecase.CreateSurveyFormUseCase;
//import com.ohgiraffer.survey.presentation.api.request.CreateSurveyFormRequest;
//import com.ohgiraffer.survey.presentation.api.response.CreateSurveyFormResponse;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/survey-forms")
//@RequiredArgsConstructor
//public class SurveyFormController {
//
//    private final CreateSurveyFormUseCase createSurveyFormUseCase;
//
//    @PostMapping
//    @PreAuthorize("hasAnyRole('MANAGER', 'INSTRUCTOR')")
//    public ResponseEntity<CreateSurveyFormResponse> createSurveyForm(
//            @Valid @RequestBody CreateSurveyFormRequest request,
//            @AuthenticationPrincipal CustomUserPrincipal principal
//    ) {
//        CreateSurveyFormCommand command =
//                new CreateSurveyFormCommand(
//                        request.title(),
//                        request.dueAt(),
//                        principal.id()
//                );
//
//        CreateSurveyFormResult result =
//                createSurveyFormUseCase.create(command);
//
//        return ResponseEntity
//                .status(HttpStatus.CREATED)
//                .body(CreateSurveyFormResponse.from(result));
//    }
//}