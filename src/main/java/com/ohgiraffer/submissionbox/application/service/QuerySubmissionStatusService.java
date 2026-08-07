package com.ohgiraffer.submissionbox.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.submission.domain.model.Submission;
import com.ohgiraffer.submission.domain.repository.SubmissionRepository;
import com.ohgiraffer.submissionbox.application.usecase.GetSubmissionStatusUseCase;
import com.ohgiraffer.submissionbox.application.usecase.SubmissionStatusDetailResult;
import com.ohgiraffer.submissionbox.application.usecase.SubmissionStatusResult;
import com.ohgiraffer.submissionbox.domain.model.SubmissionBox;
import com.ohgiraffer.submissionbox.domain.model.SubmissionTargetScope;
import com.ohgiraffer.submissionbox.domain.repository.SubmissionBoxRepository;
import com.ohgiraffer.submissionbox.application.port.SubmissionTeamTargetPort;
import com.ohgiraffer.submissionbox.application.port.TeamSubmissionTarget;
import com.ohgiraffer.submissionbox.application.usecase.SubmissionBoxItemResult;
import com.ohgiraffer.user.domain.model.UserStatus;
import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.user.domain.model.User;
import com.ohgiraffer.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuerySubmissionStatusService
        implements GetSubmissionStatusUseCase {

    private static final int MAX_PAGE_SIZE = 100;
    private final SubmissionBoxRepository submissionBoxRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final SubmissionTeamTargetPort submissionTeamTargetPort;

    @Override
    public SubmissionStatusDetailResult
    getSubmissionStatus(
            Long submissionBoxId,
            Long requesterId,
            Role requesterRole,
            String keyword,
            String status,
            int page,
            int size
    ) {
        validateRequest(
                submissionBoxId,
                requesterId,
                requesterRole,
                page,
                size
        );

        String normalizedKeyword =
                normalizeKeyword(keyword);

        String normalizedStatus =
                normalizeStatus(status);

        SubmissionBox submissionBox =
                submissionBoxRepository
                        .findById(submissionBoxId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode
                                                .SUBMISSION_BOX_NOT_FOUND
                                )
                        );

        List<SubmissionBoxItemResult> itemResults =
                submissionBox.getItems()
                        .stream()
                        .map(SubmissionBoxItemResult::from)
                        .sorted(
                                Comparator.comparingInt(
                                        SubmissionBoxItemResult::sortOrder
                                )
                        )
                        .toList();

        List<Submission> submissions =
                submissionRepository
                        .findAllBySubmissionBoxId(
                                submissionBoxId
                        );

        List<SubmissionTarget> targets =
                findSubmissionTargets(
                        submissionBox
                );

        Map<Long, Submission> submissionByTargetId =
                mapSubmissionsByTargetId(
                        submissionBox,
                        submissions
                );

        List<SubmissionStatusResult> allResults =
                targets.stream()
                        .map(target ->
                                toSubmissionStatusResult(
                                        target,
                                        submissionByTargetId
                                )
                        )
                        .sorted(
                                Comparator
                                        .comparing(
                                                SubmissionStatusResult
                                                        ::targetName,
                                                Comparator.nullsLast(
                                                        String
                                                                .CASE_INSENSITIVE_ORDER
                                                )
                                        )
                                        .thenComparing(
                                                SubmissionStatusResult
                                                        ::targetId,
                                                Comparator.nullsLast(
                                                        Comparator
                                                                .naturalOrder()
                                                )
                                        )
                        )
                        .toList();

        int submittedCount =
                Math.toIntExact(
                        allResults.stream()
                                .filter(
                                        SubmissionStatusResult::submitted
                                )
                                .count()
                );

        List<SubmissionStatusResult> filteredResults =
                allResults.stream()
                        .filter(result ->
                                matchesKeyword(
                                        result,
                                        normalizedKeyword
                                )
                        )
                        .filter(result ->
                                matchesStatus(
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

        List<SubmissionStatusResult> pagedResults =
                getPage(
                        filteredResults,
                        page,
                        size
                );

        return new SubmissionStatusDetailResult(
                submissionBox.getId(),
                submissionBox.getProjectName(),
                submissionBox.getTargetScope(),
                submissionBox.getStartAt(),
                submissionBox.getDueAt(),
                submissionBox.getLatePolicy(),
                submittedCount,
                allResults.size(),
                itemResults,
                page,
                size,
                filteredCount,
                totalPages,
                pagedResults
        );
    }

    private void validateRequest(
            Long submissionBoxId,
            Long requesterId,
            Role requesterRole,
            int page,
            int size
    ) {
        if (submissionBoxId == null
                || submissionBoxId <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "제출함 ID가 올바르지 않습니다."
            );
        }

        if (requesterId == null
                || requesterId <= 0
                || requesterRole == null) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_BOX_ACCESS_DENIED
            );
        }

        boolean staff =
                requesterRole == Role.MANAGER
                        || requesterRole
                        == Role.INSTRUCTOR;

        if (!staff) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_BOX_ACCESS_DENIED
            );
        }

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

    private String normalizeKeyword(
            String keyword
    ) {
        if (keyword == null
                || keyword.isBlank()) {
            return "";
        }

        return keyword.trim()
                .toLowerCase(Locale.ROOT);
    }

    private String normalizeStatus(
            String status
    ) {
        if (status == null
                || status.isBlank()) {
            return "ALL";
        }

        String normalized =
                status.trim()
                        .toUpperCase(Locale.ROOT);

        if (!normalized.equals("ALL")
                && !normalized.equals("SUBMITTED")
                && !normalized.equals("NOT_SUBMITTED")) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "제출 상태는 ALL, SUBMITTED, NOT_SUBMITTED 중 하나여야 합니다."
            );
        }

        return normalized;
    }


    private boolean matchesKeyword(
            SubmissionStatusResult result,
            String keyword
    ) {
        if (keyword.isBlank()) {
            return true;
        }

        String targetName =
                result.targetName() == null
                        ? ""
                        : result.targetName()
                        .toLowerCase(
                                Locale.ROOT
                        );

        return targetName.contains(keyword);
    }

    private boolean matchesStatus(
            SubmissionStatusResult result,
            String status
    ) {
        return switch (status) {
            case "SUBMITTED" ->
                    result.submitted();

            case "NOT_SUBMITTED" ->
                    !result.submitted();

            default -> true;
        };
    }

    private int calculateTotalPages(
            long filteredCount,
            int size
    ) {
        if (filteredCount == 0) {
            return 0;
        }

        return (int) (
                (filteredCount + size - 1)
                        / size
        );
    }

    private List<SubmissionStatusResult> getPage(
            List<SubmissionStatusResult> results,
            int page,
            int size
    ) {
        long fromIndexLong =
                (long) page * size;

        if (fromIndexLong
                >= results.size()) {
            return List.of();
        }

        int fromIndex =
                (int) fromIndexLong;

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

    private List<SubmissionTarget> findSubmissionTargets(
            SubmissionBox submissionBox
    ) {
        if (submissionBox.getTargetScope()
                == SubmissionTargetScope.INDIVIDUAL) {
            return findIndividualTargets();
        }

        return findTeamTargets();
    }

    private List<SubmissionTarget> findIndividualTargets() {
        return userRepository
                .findAllByRoleAndStatus(
                        Role.STUDENT,
                        UserStatus.ACTIVE
                )
                .stream()
                .map(user ->
                        new SubmissionTarget(
                                user.getId(),
                                resolveStudentName(user)
                        )
                )
                .toList();
    }

    private List<SubmissionTarget> findTeamTargets() {
        return submissionTeamTargetPort
                .findActiveTeams()
                .stream()
                .map(team ->
                        new SubmissionTarget(
                                team.teamId(),
                                team.teamName()
                        )
                )
                .toList();
    }

    private String resolveStudentName(
            User user
    ) {
        if (user.getName() == null
                || user.getName().isBlank()) {
            return "훈련생 " + user.getId();
        }

        return user.getName().trim();
    }

    private Map<Long, Submission> mapSubmissionsByTargetId(
            SubmissionBox submissionBox,
            List<Submission> submissions
    ) {
        return submissions.stream()
                .collect(
                        Collectors.toMap(
                                submission ->
                                        resolveSubmissionTargetId(
                                                submissionBox,
                                                submission
                                        ),
                                submission -> submission,
                                (first, second) -> {
                                    throw new BusinessException(
                                            ErrorCode
                                                    .SUBMISSION_TEAM_DATA_INCONSISTENT
                                    );
                                }
                        )
                );
    }

    private Long resolveSubmissionTargetId(
            SubmissionBox submissionBox,
            Submission submission
    ) {
        Long targetId;

        if (submissionBox.getTargetScope()
                == SubmissionTargetScope.TEAM) {
            targetId = submission.getTeamId();
        } else {
            targetId = submission.getOwnerUserId();
        }

        if (targetId == null
                || targetId <= 0) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_TEAM_DATA_INCONSISTENT
            );
        }

        return targetId;
    }

    private SubmissionStatusResult toSubmissionStatusResult(
            SubmissionTarget target,
            Map<Long, Submission> submissionByTargetId
    ) {
        Submission submission =
                submissionByTargetId.get(
                        target.targetId()
                );

        if (submission == null) {
            return SubmissionStatusResult.notSubmitted(
                    target.targetId(),
                    target.targetName(),
                    false,
                    false
            );
        }

        return SubmissionStatusResult.submitted(
                submission,
                target.targetName(),
                false,
                false
        );
    }

    private record SubmissionTarget(
            Long targetId,
            String targetName
    ) {
    }
}