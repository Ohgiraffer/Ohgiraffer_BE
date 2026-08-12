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
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

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
    @Transactional(readOnly = true)
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

        List<StudentSubmissionHistoryItemResult> items =
                new ArrayList<>();

        items.addAll(
                getSubmissionHistoryItems(
                        studentId
                )
        );

        items.addAll(
                getSurveyHistoryItems(
                        student.getEmail()
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
            Long studentId
    ) {
        return submissionBoxRepository
                .findAll()
                .stream()
                .map(submissionBox ->
                        toSubmissionHistoryItem(
                                submissionBox,
                                studentId
                        )
                )
                /*
                 * 팀 제출함 시작일시에 학생이 어떤 팀에도 속하지 않았다면
                 * 해당 제출함은 학생의 제출 대상이 아니므로 제외합니다.
                 */
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
            String studentEmail
    ) {
        String normalizedStudentEmail =
                normalizeEmail(studentEmail);

        return surveyFormRepository
                .findAll()
                .stream()
                /*
                 * DRAFT는 학생에게 공개되지 않은 설문이므로
                 * 개인 응답 이력에서 제외합니다.
                 */
                .filter(surveyForm ->
                        surveyForm.getStatus()
                                != SurveyFormStatus.DRAFT
                )
                .map(surveyForm ->
                        toSurveyHistoryItem(
                                surveyForm,
                                normalizedStudentEmail
                        )
                )
                .toList();
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