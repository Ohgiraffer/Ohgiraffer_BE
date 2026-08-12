package com.ohgiraffer.submission.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.submission.application.usecase.GetStudentSubmissionHistoryUseCase;
import com.ohgiraffer.submission.application.usecase.StudentSubmissionHistoryItemResult;
import com.ohgiraffer.submission.application.usecase.StudentSubmissionHistoryResult;
import com.ohgiraffer.submission.application.usecase.StudentSubmissionHistorySourceType;
import com.ohgiraffer.submission.application.usecase.StudentSubmissionHistoryStatus;
import com.ohgiraffer.submission.domain.model.Submission;
import com.ohgiraffer.submission.domain.repository.StudentTeamRepository;
import com.ohgiraffer.submission.domain.repository.SubmissionRepository;
import com.ohgiraffer.submissionbox.domain.model.SubmissionBox;
import com.ohgiraffer.submissionbox.domain.model.SubmissionTargetScope;
import com.ohgiraffer.submissionbox.domain.repository.SubmissionBoxRepository;
import com.ohgiraffer.survey.application.port.GoogleFormPort;
import com.ohgiraffer.survey.application.port.GoogleFormResponseInfo;
import com.ohgiraffer.survey.domain.model.SurveyForm;
import com.ohgiraffer.survey.domain.model.SurveyFormStatus;
import com.ohgiraffer.survey.domain.repository.SurveyFormRepository;
import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.user.domain.model.User;
import com.ohgiraffer.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueryStudentSubmissionHistoryService
        implements GetStudentSubmissionHistoryUseCase {

    /*
     * Submission의 submittedAt은 애플리케이션 기준 LocalDateTime이고,
     * Google Forms 응답 시각은 Instant이므로 서울 시간으로 변환합니다.
     */
    private static final ZoneId SERVICE_ZONE =
            ZoneId.of("Asia/Seoul");

    private final UserRepository userRepository;
    private final SubmissionBoxRepository submissionBoxRepository;
    private final SubmissionRepository submissionRepository;
    private final StudentTeamRepository studentTeamRepository;
    private final SurveyFormRepository surveyFormRepository;
    private final GoogleFormPort googleFormPort;

    @Override
    public StudentSubmissionHistoryResult getHistory(
            Long studentId,
            Long requesterId,
            Role requesterRole
    ) {
        validateRequest(
                studentId,
                requesterId,
                requesterRole
        );

        User requester =
                findUser(requesterId);

        User student =
                findStudent(studentId);

        validateSameBootcamp(
                requester,
                student
        );

        Long bootcampId = student.getBootcampId();

        if (bootcampId == null) {
            throw new BusinessException(
                    ErrorCode.BOOTCAMP_ACCESS_DENIED
            );
        }

        /*
         * 먼저 현재 부트캠프에 해당하는 DB 데이터를 모두 조회합니다.
         * 이 시점에는 아직 Google Forms API를 호출하지 않습니다.
         */
        List<SubmissionBox> submissionBoxes =
                submissionBoxRepository
                        .findAllByBootcampId(
                                bootcampId
                        );

        List<SurveyForm> surveyForms =
                surveyFormRepository
                        .findAllByBootcampId(
                                bootcampId
                        );

        List<StudentSubmissionHistoryItemResult> items =
                new ArrayList<>();

        /*
         * 제출 이력에 필요한 DB 조회를 먼저 처리합니다.
         */
        items.addAll(
                getSubmissionHistoryItems(
                        studentId,
                        submissionBoxes
                )
        );

        /*
         * DB 관련 조회가 끝난 뒤 Google Forms 응답을 조회합니다.
         */
        items.addAll(
                getSurveyHistoryItems(
                        student.getEmail(),
                        surveyForms
                )
        );

        List<StudentSubmissionHistoryItemResult> sortedItems =
                items.stream()
                        .sorted(
                                Comparator.comparing(
                                        StudentSubmissionHistoryItemResult
                                                ::dueAt,
                                        Comparator.nullsLast(
                                                Comparator.reverseOrder()
                                        )
                                )
                        )
                        .toList();

        return StudentSubmissionHistoryResult.of(
                student.getId(),
                student.getName(),
                student.getEmail(),
                sortedItems
        );
    }

    private void validateRequest(
            Long studentId,
            Long requesterId,
            Role requesterRole
    ) {
        if (studentId == null || studentId <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "훈련생 ID가 올바르지 않습니다."
            );
        }

        if (requesterId == null
                || requesterId <= 0
                || requesterRole == null) {
            throw new BusinessException(
                    ErrorCode.FORBIDDEN
            );
        }

        if (requesterRole != Role.MANAGER
                && requesterRole != Role.INSTRUCTOR) {
            throw new BusinessException(
                    ErrorCode.FORBIDDEN
            );
        }
    }

    private User findUser(
            Long userId
    ) {
        return userRepository
                .findById(userId)
                .orElseThrow(
                        () -> new BusinessException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );
    }

    private User findStudent(
            Long studentId
    ) {
        User student =
                findUser(studentId);

        if (student.getRole() != Role.STUDENT) {
            throw new BusinessException(
                    ErrorCode.USER_NOT_FOUND,
                    "조회 대상은 훈련생이어야 합니다."
            );
        }

        return student;
    }

    private void validateSameBootcamp(
            User requester,
            User student
    ) {
        Long requesterBootcampId =
                requester.getBootcampId();

        Long studentBootcampId =
                student.getBootcampId();

        if (requesterBootcampId == null
                || studentBootcampId == null
                || !requesterBootcampId.equals(
                studentBootcampId
        )) {
            throw new BusinessException(
                    ErrorCode.BOOTCAMP_ACCESS_DENIED
            );
        }
    }

    private List<StudentSubmissionHistoryItemResult>
    getSubmissionHistoryItems(
            Long studentId,
            List<SubmissionBox> submissionBoxes
    ) {
        return submissionBoxes
                .stream()
                .map(submissionBox ->
                        toSubmissionHistoryItem(
                                submissionBox,
                                studentId
                        )
                )
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<StudentSubmissionHistoryItemResult>
    toSubmissionHistoryItem(
            SubmissionBox submissionBox,
            Long studentId
    ) {
        Optional<Submission> submission;

        if (submissionBox.getTargetScope()
                == SubmissionTargetScope.INDIVIDUAL) {
            submission =
                    submissionRepository
                            .findBySubmissionBoxIdAndOwnerUserId(
                                    submissionBox.getId(),
                                    studentId
                            );
        } else {
            Optional<Long> teamId =
                    studentTeamRepository
                            .findTeamIdByStudentIdAndDateTime(
                                    studentId,
                                    submissionBox.getStartAt()
                            );

            if (teamId.isEmpty()) {
                return Optional.empty();
            }

            submission =
                    submissionRepository
                            .findBySubmissionBoxIdAndTeamId(
                                    submissionBox.getId(),
                                    teamId.get()
                            );
        }

        boolean completed =
                submission.isPresent();

        return Optional.of(
                new StudentSubmissionHistoryItemResult(
                        StudentSubmissionHistorySourceType
                                .SUBMISSION_BOX,
                        submissionBox.getId(),
                        submissionBox.getProjectName(),
                        submissionBox.getTargetScope(),
                        completed
                                ? StudentSubmissionHistoryStatus
                                .SUBMITTED
                                : StudentSubmissionHistoryStatus
                                .NOT_SUBMITTED,
                        completed,
                        submission
                                .map(Submission::getSubmittedAt)
                                .orElse(null),
                        submissionBox.getDueAt(),
                        submission
                                .map(Submission::isLate)
                                .orElse(false)
                )
        );
    }

    private List<StudentSubmissionHistoryItemResult>
    getSurveyHistoryItems(
            String studentEmail,
            List<SurveyForm> surveyForms
    ) {
        String normalizedStudentEmail =
                normalizeEmail(studentEmail);

        return surveyForms
                .stream()
                .filter(surveyForm ->
                        surveyForm.getStatus()
                                != SurveyFormStatus.DRAFT
                )
                .map(surveyForm ->
                        toSurveyHistoryItemSafely(
                                surveyForm,
                                normalizedStudentEmail
                        )
                )
                .toList();
    }

    private StudentSubmissionHistoryItemResult
    toSurveyHistoryItemSafely(
            SurveyForm surveyForm,
            String normalizedStudentEmail
    ) {
        try {
            return toSurveyHistoryItem(
                    surveyForm,
                    normalizedStudentEmail
            );

        } catch (BusinessException exception) {
            if (!isGoogleFormsFailure(
                    exception.getErrorCode()
            )) {
                /*
                 * Google Forms 장애가 아닌 내부 비즈니스 오류는
                 * 숨기지 않고 기존 방식대로 상위로 전달합니다.
                 */
                throw exception;
            }

            log.warn(
                    "Google Forms 응답 조회 실패. "
                            + "surveyFormId={}, googleFormId={}, errorCode={}",
                    surveyForm.getId(),
                    surveyForm.getGoogleFormId(),
                    exception.getErrorCode().getCode(),
                    exception
            );

            return new StudentSubmissionHistoryItemResult(
                    StudentSubmissionHistorySourceType
                            .SURVEY_FORM,
                    surveyForm.getId(),
                    surveyForm.getTitle(),
                    null,
                    StudentSubmissionHistoryStatus
                            .RESPONSE_CHECK_FAILED,
                    false,
                    null,
                    surveyForm.getDueAt(),
                    false
            );
        }
    }

    private boolean isGoogleFormsFailure(
            ErrorCode errorCode
    ) {
        return errorCode
                == ErrorCode.GOOGLE_FORM_ACCESS_DENIED
                || errorCode
                == ErrorCode.GOOGLE_FORM_NOT_FOUND
                || errorCode
                == ErrorCode.GOOGLE_FORM_RATE_LIMIT_EXCEEDED
                || errorCode
                == ErrorCode.GOOGLE_FORM_API_ERROR;
    }

    private StudentSubmissionHistoryItemResult
    toSurveyHistoryItem(
            SurveyForm surveyForm,
            String normalizedStudentEmail
    ) {
        Optional<GoogleFormResponseInfo> latestResponse =
                findLatestResponse(
                        surveyForm.getGoogleFormId(),
                        normalizedStudentEmail
                );

        boolean completed =
                latestResponse.isPresent();

        return new StudentSubmissionHistoryItemResult(
                StudentSubmissionHistorySourceType
                        .SURVEY_FORM,
                surveyForm.getId(),
                surveyForm.getTitle(),
                null,
                completed
                        ? StudentSubmissionHistoryStatus
                        .RESPONDED
                        : StudentSubmissionHistoryStatus
                        .NOT_RESPONDED,
                completed,
                latestResponse
                        .map(
                                GoogleFormResponseInfo
                                        ::submittedAt
                        )
                        .map(this::toLocalDateTime)
                        .orElse(null),
                surveyForm.getDueAt(),
                false
        );
    }

    private Optional<GoogleFormResponseInfo>
    findLatestResponse(
            String googleFormId,
            String normalizedStudentEmail
    ) {
        if (normalizedStudentEmail.isBlank()) {
            return Optional.empty();
        }

        return googleFormPort
                .getResponses(googleFormId)
                .stream()
                .filter(response ->
                        normalizeEmail(
                                response.respondentEmail()
                        ).equals(
                                normalizedStudentEmail
                        )
                )
                .max(
                        Comparator.comparing(
                                GoogleFormResponseInfo
                                        ::submittedAt,
                                Comparator.nullsFirst(
                                        Comparator.naturalOrder()
                                )
                        )
                );
    }

    private LocalDateTime toLocalDateTime(
            Instant instant
    ) {
        return instant == null
                ? null
                : LocalDateTime.ofInstant(
                instant,
                SERVICE_ZONE
        );
    }

    private String normalizeEmail(
            String email
    ) {
        return email == null
                ? ""
                : email.trim()
                .toLowerCase(Locale.ROOT);
    }
}