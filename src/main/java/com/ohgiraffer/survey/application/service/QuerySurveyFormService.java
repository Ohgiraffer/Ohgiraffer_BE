package com.ohgiraffer.survey.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.survey.application.port.GoogleFormPort;
import com.ohgiraffer.survey.application.port.GoogleFormResponseInfo;
import com.ohgiraffer.survey.application.usecase.GetSurveyFormDetailUseCase;
import com.ohgiraffer.survey.application.usecase.GetSurveyFormListUseCase;
import com.ohgiraffer.survey.application.usecase.GetSurveyResponsesUseCase;
import com.ohgiraffer.survey.application.usecase.StudentSurveyResponseResult;
import com.ohgiraffer.survey.application.usecase.SurveyFormDetailResult;
import com.ohgiraffer.survey.application.usecase.SurveyFormListResult;
import com.ohgiraffer.survey.application.usecase.SurveyResponseDetailResult;
import com.ohgiraffer.survey.application.usecase.SurveyResponseStatus;
import com.ohgiraffer.survey.domain.model.SurveyForm;
import com.ohgiraffer.survey.domain.repository.SurveyFormRepository;
import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.user.domain.model.User;
import com.ohgiraffer.user.domain.model.UserStatus;
import com.ohgiraffer.user.domain.repository.UserRepository;
import com.ohgiraffer.survey.domain.model.SurveyFormStatus;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class QuerySurveyFormService
        implements GetSurveyFormListUseCase,
        GetSurveyFormDetailUseCase,
        GetSurveyResponsesUseCase {

    private static final int MAX_PAGE_SIZE = 100;

    private final SurveyFormRepository surveyFormRepository;
    private final UserRepository userRepository;
    private final GoogleFormPort googleFormPort;

    public QuerySurveyFormService(
            SurveyFormRepository surveyFormRepository,
            UserRepository userRepository,
            GoogleFormPort googleFormPort
    ) {
        this.surveyFormRepository = surveyFormRepository;
        this.userRepository = userRepository;
        this.googleFormPort = googleFormPort;
    }

    @Override
    public List<SurveyFormListResult> getSurveyForms(
            Long userId,
            String userEmail,
            Role role
    ) {
        List<User> activeStudents =
                findActiveStudents();

        Set<String> studentEmails =
                activeStudents.stream()
                        .map(User::getEmail)
                        .map(this::normalizeEmail)
                        .filter(email -> !email.isBlank())
                        .collect(Collectors.toSet());

        String normalizedUserEmail =
                normalizeEmail(userEmail);

        return surveyFormRepository.findAll()
                .stream()
                .filter(surveyForm ->
                        canViewSurvey(
                                surveyForm,
                                role
                        )
                )
                .map(surveyForm -> {
                    List<GoogleFormResponseInfo> responses =
                            googleFormPort.getResponses(
                                    surveyForm.getGoogleFormId()
                            );

                    Set<String> respondentEmails =
                            responses.stream()
                                    .map(GoogleFormResponseInfo::respondentEmail)
                                    .map(this::normalizeEmail)
                                    .filter(email -> !email.isBlank())
                                    .collect(Collectors.toSet());

                    int respondedCount =
                            (int) studentEmails.stream()
                                    .filter(respondentEmails::contains)
                                    .count();

                    Boolean responded =
                            role == Role.STUDENT
                                    ? respondentEmails.contains(
                                    normalizedUserEmail
                            )
                                    : null;

                    String responseUrl =
                            role == Role.STUDENT
                                    ? surveyForm.getResponseUrl()
                                    : null;

                    return SurveyFormListResult.from(
                            surveyForm,
                            respondedCount,
                            studentEmails.size(),
                            responded,
                            responseUrl
                    );
                })
                .toList();
    }

    private boolean canViewSurvey(
            SurveyForm surveyForm,
            Role role
    ) {
        if (role != Role.STUDENT) {
            return true;
        }

        return surveyForm.getStatus()
                == SurveyFormStatus.PUBLISHED
                && !LocalDateTime.now()
                .isAfter(surveyForm.getDueAt());
    }

    @Override
    public SurveyFormDetailResult getSurveyForm(
            Long surveyFormId
    ) {
        SurveyForm surveyForm =
                findSurveyForm(surveyFormId);

        return SurveyFormDetailResult.from(
                surveyForm
        );
    }

    @Override
    public SurveyResponseDetailResult getSurveyResponses(
            Long surveyFormId,
            String keyword,
            SurveyResponseStatus responseStatus,
            int page,
            int size
    ) {
        validatePageRequest(
                page,
                size
        );

        SurveyForm surveyForm =
                findSurveyForm(surveyFormId);

        SurveyResponseStatus normalizedStatus =
                responseStatus == null
                        ? SurveyResponseStatus.ALL
                        : responseStatus;

        String normalizedKeyword =
                keyword == null
                        ? ""
                        : keyword
                        .trim()
                        .toLowerCase(Locale.ROOT);

        List<User> students =
                findActiveStudents();

        Map<String, GoogleFormResponseInfo> responseByEmail =
                getResponseByEmail(
                        surveyForm.getGoogleFormId()
                );

        List<StudentSurveyResponseResult> allStudentResults =
                students
                        .stream()
                        .map(student ->
                                toStudentResponseResult(
                                        student,
                                        responseByEmail
                                )
                        )
                        .sorted(
                                Comparator
                                        .comparing(
                                                StudentSurveyResponseResult::name,
                                                Comparator.nullsLast(
                                                        String.CASE_INSENSITIVE_ORDER
                                                )
                                        )
                                        .thenComparing(
                                                StudentSurveyResponseResult::userId
                                        )
                        )
                        .toList();

        int respondedCount =
                Math.toIntExact(
                        allStudentResults
                                .stream()
                                .filter(
                                        StudentSurveyResponseResult::responded
                                )
                                .count()
                );

        List<StudentSurveyResponseResult> filteredResults =
                allStudentResults
                        .stream()
                        .filter(result ->
                                matchesKeyword(
                                        result,
                                        normalizedKeyword
                                )
                        )
                        .filter(result ->
                                matchesResponseStatus(
                                        result,
                                        normalizedStatus
                                )
                        )
                        .toList();

        long filteredCount =
                filteredResults.size();

        int totalPages =
                calculateTotalPages(
                        filteredCount,
                        size
                );

        List<StudentSurveyResponseResult> pagedResults =
                getPage(
                        filteredResults,
                        page,
                        size
                );

        return new SurveyResponseDetailResult(
                surveyForm.getId(),
                surveyForm.getTitle(),
                surveyForm.getStatus(),
                surveyForm.getDueAt(),
                respondedCount,
                students.size(),
                page,
                size,
                filteredCount,
                totalPages,
                pagedResults
        );
    }

    private SurveyForm findSurveyForm(
            Long surveyFormId
    ) {
        validateSurveyFormId(
                surveyFormId
        );

        return surveyFormRepository
                .findById(surveyFormId)
                .orElseThrow(
                        () -> new BusinessException(
                                ErrorCode.SURVEY_FORM_NOT_FOUND
                        )
                );
    }

    private List<User> findActiveStudents() {
        return userRepository
                .findAllByRoleAndStatus(
                        Role.STUDENT,
                        UserStatus.ACTIVE
                );
    }

    private int countStudentRespondents(
            String googleFormId,
            Set<String> studentEmails
    ) {
        return Math.toIntExact(
                googleFormPort
                        .getResponses(googleFormId)
                        .stream()
                        .map(
                                GoogleFormResponseInfo::respondentEmail
                        )
                        .filter(email ->
                                email != null
                                        && !email.isBlank()
                        )
                        .map(this::normalizeEmail)
                        .filter(studentEmails::contains)
                        .distinct()
                        .count()
        );
    }

    private Map<String, GoogleFormResponseInfo> getResponseByEmail(
            String googleFormId
    ) {
        return googleFormPort
                .getResponses(googleFormId)
                .stream()
                .filter(response ->
                        response.respondentEmail() != null
                                && !response
                                .respondentEmail()
                                .isBlank()
                )
                .collect(
                        Collectors.toMap(
                                response ->
                                        normalizeEmail(
                                                response.respondentEmail()
                                        ),
                                Function.identity(),
                                this::selectLatestResponse
                        )
                );
    }

    private GoogleFormResponseInfo selectLatestResponse(
            GoogleFormResponseInfo first,
            GoogleFormResponseInfo second
    ) {
        Instant firstSubmittedAt =
                first.submittedAt();

        Instant secondSubmittedAt =
                second.submittedAt();

        if (firstSubmittedAt == null) {
            return second;
        }

        if (secondSubmittedAt == null) {
            return first;
        }

        return secondSubmittedAt.isAfter(firstSubmittedAt)
                ? second
                : first;
    }

    private StudentSurveyResponseResult toStudentResponseResult(
            User student,
            Map<String, GoogleFormResponseInfo> responseByEmail
    ) {
        String normalizedEmail =
                normalizeEmail(
                        student.getEmail()
                );

        GoogleFormResponseInfo response =
                normalizedEmail.isBlank()
                        ? null
                        : responseByEmail.get(
                        normalizedEmail
                );

        return new StudentSurveyResponseResult(
                student.getId(),
                student.getName(),
                student.getEmail(),
                response != null,
                response == null
                        ? null
                        : response.submittedAt()
        );
    }

    private boolean matchesKeyword(
            StudentSurveyResponseResult result,
            String keyword
    ) {
        if (keyword.isBlank()) {
            return true;
        }

        String name =
                result.name() == null
                        ? ""
                        : result.name()
                        .toLowerCase(Locale.ROOT);

        String email =
                result.email() == null
                        ? ""
                        : result.email()
                        .toLowerCase(Locale.ROOT);

        return name.contains(keyword)
                || email.contains(keyword);
    }

    private boolean matchesResponseStatus(
            StudentSurveyResponseResult result,
            SurveyResponseStatus responseStatus
    ) {
        return switch (responseStatus) {
            case ALL -> true;
            case RESPONDED -> result.responded();
            case NOT_RESPONDED -> !result.responded();
        };
    }

    private List<StudentSurveyResponseResult> getPage(
            List<StudentSurveyResponseResult> results,
            int page,
            int size
    ) {
        long startIndex =
                (long) page * size;

        if (startIndex >= results.size()) {
            return List.of();
        }

        int fromIndex =
                Math.toIntExact(startIndex);

        int toIndex =
                Math.min(
                        fromIndex + size,
                        results.size()
                );

        return results.subList(
                fromIndex,
                toIndex
        );
    }

    private int calculateTotalPages(
            long totalCount,
            int size
    ) {
        if (totalCount == 0) {
            return 0;
        }

        return Math.toIntExact(
                (totalCount + size - 1) / size
        );
    }

    private String normalizeEmail(
            String email
    ) {
        if (email == null || email.isBlank()) {
            return "";
        }

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private void validateSurveyFormId(
            Long surveyFormId
    ) {
        if (surveyFormId == null
                || surveyFormId <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "설문 폼 ID가 올바르지 않습니다."
            );
        }
    }

    private void validatePageRequest(
            int page,
            int size
    ) {
        if (page < 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "페이지 번호는 0 이상이어야 합니다."
            );
        }

        if (size <= 0
                || size > MAX_PAGE_SIZE) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "페이지 크기는 1 이상 100 이하여야 합니다."
            );
        }
    }
}