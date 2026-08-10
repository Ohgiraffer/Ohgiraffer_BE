package com.ohgiraffer.submissionbox.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.submissionbox.application.usecase.GetSubmissionBoxDetailUseCase;
import com.ohgiraffer.submissionbox.application.usecase.GetSubmissionBoxListUseCase;
import com.ohgiraffer.submissionbox.application.usecase.SubmissionBoxDetailResult;
import com.ohgiraffer.submissionbox.application.usecase.SubmissionBoxListResult;
import com.ohgiraffer.submissionbox.domain.model.SubmissionBox;
import com.ohgiraffer.submissionbox.domain.repository.SubmissionBoxRepository;
import com.ohgiraffer.submission.domain.model.Submission;
import com.ohgiraffer.submission.domain.repository.StudentTeamRepository;
import com.ohgiraffer.submission.domain.repository.SubmissionRepository;
import com.ohgiraffer.submissionbox.domain.model.SubmissionTargetScope;
import com.ohgiraffer.submissionbox.application.port.SubmissionTeamTargetPort;
import com.ohgiraffer.user.domain.model.UserStatus;
import com.ohgiraffer.user.domain.repository.UserRepository;
import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.submissionbox.application.usecase.SubmissionStatusResult;
import com.ohgiraffer.submission.domain.model.SubmissionListEntry;
import java.util.Map;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;




@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuerySubmissionBoxService
        implements GetSubmissionBoxListUseCase,
        GetSubmissionBoxDetailUseCase {

    private final SubmissionBoxRepository submissionBoxRepository;
    private final SubmissionRepository submissionRepository;
    private final StudentTeamRepository studentTeamRepository;
    private final UserRepository userRepository;
    private final SubmissionTeamTargetPort submissionTeamTargetPort;
    private final Clock clock;

    @Override
    public List<SubmissionBoxListResult> getSubmissionBoxes(
            Long userId,
            Role role
    ) {
        LocalDateTime now =
                LocalDateTime.now(clock);

        List<SubmissionBox> submissionBoxes =
                submissionBoxRepository.findAll();

        if (submissionBoxes.isEmpty()) {
            return List.of();
        }

        List<Long> submissionBoxIds =
                submissionBoxes.stream()
                        .map(SubmissionBox::getId)
                        .toList();

        List<SubmissionListEntry> submissionEntries =
                submissionRepository
                        .findListEntriesBySubmissionBoxIds(
                                submissionBoxIds
                        );

        Map<Long, List<SubmissionListEntry>>
                entriesBySubmissionBoxId =
                submissionEntries.stream()
                        .collect(Collectors.groupingBy(
                                SubmissionListEntry
                                        ::submissionBoxId
                        ));

        if (role == Role.STUDENT) {
            return getStudentSubmissionBoxes(
                    submissionBoxes,
                    entriesBySubmissionBoxId,
                    userId,
                    now
            );
        }

        return getStaffSubmissionBoxes(
                submissionBoxes,
                entriesBySubmissionBoxId,
                now
        );
    }

    private List<SubmissionBoxListResult>
    getStudentSubmissionBoxes(
            List<SubmissionBox> submissionBoxes,
            Map<Long, List<SubmissionListEntry>>
                    entriesBySubmissionBoxId,
            Long studentId,
            LocalDateTime now
    ) {
        Optional<Long> activeTeamId =
                studentTeamRepository
                        .findActiveTeamIdByStudentId(
                                studentId
                        );

        return submissionBoxes.stream()
                .map(submissionBox -> {
                    List<SubmissionListEntry> entries =
                            entriesBySubmissionBoxId
                                    .getOrDefault(
                                            submissionBox.getId(),
                                            List.of()
                                    );

                    Long submissionId =
                            findStudentSubmissionId(
                                    submissionBox,
                                    entries,
                                    studentId,
                                    activeTeamId
                            );

                    return SubmissionBoxListResult
                            .forStudent(
                                    submissionBox,
                                    now,
                                    submissionId
                            );
                })
                .toList();
    }

    private List<SubmissionBoxListResult>
    getStaffSubmissionBoxes(
            List<SubmissionBox> submissionBoxes,
            Map<Long, List<SubmissionListEntry>>
                    entriesBySubmissionBoxId,
            LocalDateTime now
    ) {
        Set<Long> activeStudentIds =
                userRepository
                        .findAllByRoleAndStatus(
                                Role.STUDENT,
                                UserStatus.ACTIVE
                        )
                        .stream()
                        .map(user -> user.getId())
                        .collect(
                                Collectors.toUnmodifiableSet()
                        );

        Set<Long> activeTeamIds =
                submissionTeamTargetPort
                        .findActiveTeams()
                        .stream()
                        .map(team -> team.teamId())
                        .collect(
                                Collectors.toUnmodifiableSet()
                        );

        return submissionBoxes.stream()
                .map(submissionBox -> {
                    boolean teamSubmission =
                            submissionBox.getTargetScope()
                                    == SubmissionTargetScope.TEAM;

                    Set<Long> activeTargetIds =
                            teamSubmission
                                    ? activeTeamIds
                                    : activeStudentIds;

                    List<SubmissionListEntry> entries =
                            entriesBySubmissionBoxId
                                    .getOrDefault(
                                            submissionBox.getId(),
                                            List.of()
                                    );

                    int submittedCount =
                            calculateSubmittedCount(
                                    entries,
                                    teamSubmission,
                                    activeTargetIds
                            );

                    int targetCount =
                            activeTargetIds.size();

                    return SubmissionBoxListResult
                            .forStaff(
                                    submissionBox,
                                    now,
                                    submittedCount,
                                    targetCount
                            );
                })
                .toList();
    }

    private int calculateSubmittedCount(
            List<SubmissionListEntry> entries,
            boolean teamSubmission,
            Set<Long> activeTargetIds
    ) {
        return Math.toIntExact(
                entries.stream()
                        .map(entry ->
                                teamSubmission
                                        ? entry.teamId()
                                        : entry.ownerUserId()
                        )
                        .filter(targetId ->
                                targetId != null
                                        && activeTargetIds
                                        .contains(targetId)
                        )
                        .distinct()
                        .count()
        );
    }

    private Long findStudentSubmissionId(
            SubmissionBox submissionBox,
            List<SubmissionListEntry> entries,
            Long studentId,
            Optional<Long> activeTeamId
    ) {
        if (submissionBox.getTargetScope()
                == SubmissionTargetScope.INDIVIDUAL) {
            return entries.stream()
                    .filter(entry ->
                            studentId.equals(
                                    entry.ownerUserId()
                            )
                    )
                    .map(
                            SubmissionListEntry::submissionId
                    )
                    .findFirst()
                    .orElse(null);
        }

        if (activeTeamId.isEmpty()) {
            return null;
        }

        Long teamId = activeTeamId.get();

        return entries.stream()
                .filter(entry ->
                        teamId.equals(
                                entry.teamId()
                        )
                )
                .map(
                        SubmissionListEntry::submissionId
                )
                .findFirst()
                .orElse(null);
    }

    @Override
    public SubmissionBoxDetailResult getSubmissionBox(
            Long submissionBoxId,
            Long requesterId,
            Role requesterRole
    ) {
        validateDetailRequest(
                submissionBoxId,
                requesterId,
                requesterRole
        );

        SubmissionBox submissionBox =
                submissionBoxRepository
                        .findById(submissionBoxId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.SUBMISSION_BOX_NOT_FOUND
                                )
                        );

        LocalDateTime now =
                LocalDateTime.now(clock);

        SubmissionBoxDetailResult basicDetail =
                SubmissionBoxDetailResult.from(
                        submissionBox,
                        now
                );

        boolean editable =
                isEditable(
                        submissionBox,
                        now
                );

        Optional<Long> requesterTeamId =
                findRequesterTeamId(
                        submissionBox,
                        requesterId
                );

        boolean eligibleToSubmit =
                isEligibleToSubmit(
                        submissionBox,
                        requesterTeamId
                );

        List<Submission> submissions =
                submissionRepository
                        .findAllBySubmissionBoxId(
                                submissionBoxId
                        );

        List<SubmissionStatusResult> submissionStatuses =
                new ArrayList<>();

        Long mySubmissionId = null;

        for (Submission submission : submissions) {
            boolean mine =
                    isRequesterSubmission(
                            submissionBox,
                            submission,
                            requesterId,
                            requesterTeamId
                    );

            if (mine) {
                mySubmissionId =
                        submission.getId();
            }

            submissionStatuses.add(
                    SubmissionStatusResult.submitted(
                            submission,
                            createTemporaryTargetName(
                                    submissionBox,
                                    submission
                            ),
                            mine,
                            editable
                    )
            );
        }

        if (eligibleToSubmit
                && mySubmissionId == null) {
            submissionStatuses.add(
                    createMyNotSubmittedStatus(
                            submissionBox,
                            requesterId,
                            requesterTeamId,
                            basicDetail.acceptingSubmissions()
                    )
            );
        }

        return SubmissionBoxDetailResult.from(
                submissionBox,
                now,
                null,
                mySubmissionId,
                eligibleToSubmit,
                submissionStatuses
        );
    }

    private void validateDetailRequest(
            Long submissionBoxId,
            Long requesterId,
            Role requesterRole
    ) {
        if (submissionBoxId == null
                || submissionBoxId <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "제출함 ID가 올바르지 않습니다."
            );
        }

        if (requesterId == null
                || requesterId <= 0) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_BOX_ACCESS_DENIED
            );
        }

        if (requesterRole != Role.STUDENT) {
            throw new BusinessException(
                    ErrorCode.SUBMISSION_BOX_ACCESS_DENIED
            );
        }
    }

    private Optional<Long> findRequesterTeamId(
            SubmissionBox submissionBox,
            Long requesterId
    ) {
        if (submissionBox.getTargetScope()
                != SubmissionTargetScope.TEAM) {
            return Optional.empty();
        }

        return studentTeamRepository
                .findActiveTeamIdByStudentId(
                        requesterId
                );
    }

    private boolean isEligibleToSubmit(
            SubmissionBox submissionBox,
            Optional<Long> requesterTeamId
    ) {
        if (submissionBox.getTargetScope()
                == SubmissionTargetScope.INDIVIDUAL) {
            return true;
        }

        return requesterTeamId.isPresent();
    }

    private boolean isRequesterSubmission(
            SubmissionBox submissionBox,
            Submission submission,
            Long requesterId,
            Optional<Long> requesterTeamId
    ) {
        if (submissionBox.getTargetScope()
                == SubmissionTargetScope.INDIVIDUAL) {
            return requesterId.equals(
                    submission.getOwnerUserId()
            );
        }

        return requesterTeamId
                .map(teamId ->
                        teamId.equals(
                                submission.getTeamId()
                        )
                )
                .orElse(false);
    }

    private String createTemporaryTargetName(
            SubmissionBox submissionBox,
            Submission submission
    ) {
        if (submissionBox.getTargetScope()
                == SubmissionTargetScope.TEAM) {
            return "팀 " + submission.getTeamId();
        }

        return "훈련생 " + submission.getOwnerUserId();
    }

    private SubmissionStatusResult createMyNotSubmittedStatus(
            SubmissionBox submissionBox,
            Long requesterId,
            Optional<Long> requesterTeamId,
            boolean acceptingSubmissions
    ) {
        if (submissionBox.getTargetScope()
                == SubmissionTargetScope.INDIVIDUAL) {
            return SubmissionStatusResult.notSubmitted(
                    requesterId,
                    "훈련생 " + requesterId,
                    true,
                    acceptingSubmissions
            );
        }

        Long teamId =
                requesterTeamId.orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.SUBMISSION_ACCESS_DENIED
                        )
                );

        return SubmissionStatusResult.notSubmitted(
                teamId,
                "팀 " + teamId,
                true,
                acceptingSubmissions
        );
    }

    private boolean isEditable(
            SubmissionBox submissionBox,
            LocalDateTime now
    ) {
        if (now.isBefore(
                submissionBox.getStartAt()
        )) {
            return false;
        }

        return !now.isAfter(
                submissionBox.getDueAt()
        );
    }

}