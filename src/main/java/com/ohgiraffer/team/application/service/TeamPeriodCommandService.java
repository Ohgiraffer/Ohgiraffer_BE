package com.ohgiraffer.team.application.service;

import com.ohgiraffer.bootcamp.application.port.GetUserBootcampIdPort;
import com.ohgiraffer.bootcamp.domain.model.Bootcamp;
import com.ohgiraffer.bootcamp.domain.repository.BootcampRepository;
import com.ohgiraffer.global.aop.lock.DistributedLock;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.team.application.command.CreateTeamPeriodCommand;
import com.ohgiraffer.team.application.command.DeleteTeamPeriodCommand;
import com.ohgiraffer.team.application.command.UpdateTeamPeriodCommand;
import com.ohgiraffer.team.application.event.TeamExternalResourceDeleteTarget;
import com.ohgiraffer.team.application.event.TeamPeriodDeletedEvent;
import com.ohgiraffer.team.application.usecase.CreateTeamPeriodUseCase;
import com.ohgiraffer.team.application.usecase.DeleteTeamPeriodUseCase;
import com.ohgiraffer.team.application.usecase.TeamPeriodResult;
import com.ohgiraffer.team.application.usecase.UpdateTeamPeriodUseCase;
import com.ohgiraffer.team.domain.model.TeamPeriod;
import com.ohgiraffer.team.domain.repository.TeamPeriodRepository;
import com.ohgiraffer.team.domain.repository.TeamRepository;
import com.ohgiraffer.user.domain.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamPeriodCommandService
        implements CreateTeamPeriodUseCase,
        UpdateTeamPeriodUseCase,
        DeleteTeamPeriodUseCase {

    private final TeamRepository teamRepository;
    private final TeamPeriodRepository teamPeriodRepository;
    private final BootcampRepository bootcampRepository;
    private final GetUserBootcampIdPort getUserBootcampIdPort;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @DistributedLock(
            key = "'team:period:write'",
            waitTime = 30,
            leaseTime = 120,
            timeUnit = TimeUnit.SECONDS
    )
    public TeamPeriodResult createTeamPeriod(
            CreateTeamPeriodCommand command,
            Role requesterRole
    ) {
        validateManagerAccess(
                command.requesterId(),
                requesterRole
        );

        validateTeamPeriodRange(
                command.startDate(),
                command.endDate()
        );

        validatePeriodWithinRequesterBootcamp(
                command.requesterId(),
                command.startDate(),
                command.endDate()
        );

        validatePeriodNotOverlapping(
                command.startDate(),
                command.endDate()
        );

        TeamPeriod teamPeriod =
                TeamPeriod.create(
                        command.startDate(),
                        command.endDate()
                );

        TeamPeriod savedTeamPeriod =
                teamPeriodRepository.save(
                        teamPeriod
                );

        return TeamPeriodResult.from(
                savedTeamPeriod
        );
    }

    @Override
    @DistributedLock(
            key = "'team:period:write'",
            waitTime = 30,
            leaseTime = 120,
            timeUnit = TimeUnit.SECONDS
    )
    public TeamPeriodResult updateTeamPeriod(
            UpdateTeamPeriodCommand command,
            Role requesterRole
    ) {
        validateManagerAccess(
                command.requesterId(),
                requesterRole
        );

        validateTeamPeriodId(
                command.teamPeriodId()
        );

        validateTeamPeriodRange(
                command.startDate(),
                command.endDate()
        );

        validatePeriodWithinRequesterBootcamp(
                command.requesterId(),
                command.startDate(),
                command.endDate()
        );

        TeamPeriod teamPeriod =
                teamPeriodRepository.findByIdForUpdate(
                                command.teamPeriodId()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.TEAM_NOT_FOUND
                                )
                        );

        teamPeriod.validateAssignable();

        validatePeriodNotOverlappingForUpdate(
                command.startDate(),
                command.endDate(),
                teamPeriod.getId()
        );

        TeamPeriod updatedTeamPeriod =
                teamPeriod.update(
                        command.startDate(),
                        command.endDate()
                );

        TeamPeriod savedTeamPeriod =
                teamPeriodRepository.save(
                        updatedTeamPeriod
                );

        return TeamPeriodResult.from(
                savedTeamPeriod
        );
    }

    @Override
    @DistributedLock(
            key = "'team:period:write'",
            waitTime = 30,
            leaseTime = 120,
            timeUnit = TimeUnit.SECONDS
    )
    public void deleteTeamPeriod(
            DeleteTeamPeriodCommand command,
            Role requesterRole
    ) {
        validateManagerAccess(
                command.requesterId(),
                requesterRole
        );

        validateTeamPeriodId(
                command.teamPeriodId()
        );

        TeamPeriod teamPeriod =
                teamPeriodRepository.findByIdForUpdate(
                                command.teamPeriodId()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.TEAM_NOT_FOUND
                                )
                        );

        teamPeriod.validateAssignable();

        validateTeamPeriodDeletable(
                teamPeriod.getId()
        );

        List<TeamExternalResourceDeleteTarget> externalResourceDeleteTargets =
                createExternalResourceDeleteTargets(
                        teamPeriod.getId()
                );

        teamRepository.deleteMembersByTeamPeriodId(
                teamPeriod.getId()
        );

        teamRepository.deleteTeamsByTeamPeriodId(
                teamPeriod.getId()
        );

        teamPeriodRepository.deleteById(
                teamPeriod.getId()
        );

        publishTeamPeriodDeletedEvent(
                teamPeriod.getId(),
                externalResourceDeleteTargets
        );
    }

    private List<TeamExternalResourceDeleteTarget> createExternalResourceDeleteTargets(
            Long teamPeriodId
    ) {
        return teamRepository.findVisibleTeamsByPeriodId(
                        teamPeriodId
                )
                .stream()
                .map(team -> new TeamExternalResourceDeleteTarget(
                        team.getId(),
                        team.getSendbirdChannelUrl(),
                        team.getNotionPageId()
                ))
                .toList();
    }

    private void publishTeamPeriodDeletedEvent(
            Long teamPeriodId,
            List<TeamExternalResourceDeleteTarget> targets
    ) {
        if (targets.isEmpty()) {
            return;
        }

        eventPublisher.publishEvent(
                new TeamPeriodDeletedEvent(
                        teamPeriodId,
                        targets
                )
        );
    }

    private void validateManagerAccess(
            Long requesterId,
            Role requesterRole
    ) {
        if (requesterId == null
                || requesterId <= 0
                || requesterRole == null) {
            throw new BusinessException(
                    ErrorCode.TEAM_ACCESS_DENIED
            );
        }

        if (requesterRole != Role.INSTRUCTOR
                && requesterRole != Role.MANAGER) {
            throw new BusinessException(
                    ErrorCode.TEAM_ACCESS_DENIED
            );
        }
    }

    private void validateTeamPeriodId(
            Long teamPeriodId
    ) {
        if (teamPeriodId == null
                || teamPeriodId <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "팀 기간 ID가 올바르지 않습니다."
            );
        }
    }

    private void validateTeamPeriodRange(
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (startDate == null
                || endDate == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "팀 기간 시작일과 종료일을 입력해주세요."
            );
        }

        if (startDate.isAfter(endDate)) {
            throw new BusinessException(
                    ErrorCode.TEAM_INVALID_PERIOD
            );
        }
    }

    private void validatePeriodWithinRequesterBootcamp(
            Long requesterId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        Long bootcampId =
                getUserBootcampIdPort.findBootcampIdByUserId(
                                requesterId
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.BOOTCAMP_NOT_FOUND
                                )
                        );

        Bootcamp bootcamp =
                bootcampRepository.findById(
                                bootcampId
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.BOOTCAMP_NOT_FOUND
                                )
                        );

        if (startDate.isBefore(bootcamp.getStartDate())
                || endDate.isAfter(bootcamp.getEndDate())) {
            throw new BusinessException(
                    ErrorCode.INVALID_PERIOD_RANGE,
                    "팀 기간은 부트캠프 기간 내에서만 설정할 수 있습니다."
            );
        }
    }

    private void validatePeriodNotOverlapping(
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (teamPeriodRepository.existsVisiblePeriodOverlapping(
                startDate,
                endDate
        )) {
            throw new BusinessException(
                    ErrorCode.OVERLAPPING_PERIOD
            );
        }
    }

    private void validatePeriodNotOverlappingForUpdate(
            LocalDate startDate,
            LocalDate endDate,
            Long teamPeriodId
    ) {
        if (teamPeriodRepository.existsVisiblePeriodOverlappingAndIdNot(
                startDate,
                endDate,
                teamPeriodId
        )) {
            throw new BusinessException(
                    ErrorCode.OVERLAPPING_PERIOD
            );
        }
    }

    private void validateTeamPeriodDeletable(
            Long teamPeriodId
    ) {
        if (teamRepository.existsActiveMemberByTeamPeriodId(
                teamPeriodId
        )) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "팀에 배정된 훈련생이 있는 기간은 삭제할 수 없습니다."
            );
        }
    }
}