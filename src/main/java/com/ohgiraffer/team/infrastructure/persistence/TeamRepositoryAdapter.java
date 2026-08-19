package com.ohgiraffer.team.infrastructure.persistence;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.team.application.usecase.UserTeamHistoryResult;
import com.ohgiraffer.team.domain.model.Team;
import com.ohgiraffer.team.domain.model.TeamMember;
import com.ohgiraffer.team.domain.model.UnassignedStudent;
import com.ohgiraffer.team.domain.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TeamRepositoryAdapter
        implements TeamRepository {

    private static final String TEAM_NAME_UNIQUE_CONSTRAINT =
            "uq_team_period_name";

    private final SpringDataTeamRepository springDataTeamRepository;
    private final SpringDataTeamMemberRepository springDataTeamMemberRepository;

    @Override
    public Team save(
            Team team
    ) {
        try {
            TeamJpaEntity savedEntity =
                    springDataTeamRepository.saveAndFlush(
                            TeamJpaEntity.from(
                                    team
                            )
                    );

            return savedEntity.toDomain();
        } catch (DataIntegrityViolationException exception) {
            if (isTeamNameUniqueConstraintViolation(
                    exception
            )) {
                throw new BusinessException(
                        ErrorCode.TEAM_DUPLICATE_NAME
                );
            }

            throw exception;
        }
    }

    @Override
    public TeamMember saveMember(
            TeamMember teamMember
    ) {
        TeamMemberViewJpaEntity savedEntity =
                springDataTeamMemberRepository.saveAndFlush(
                        TeamMemberViewJpaEntity.from(
                                teamMember
                        )
                );

        return savedEntity.toDomain();
    }

    @Override
    public List<Team> findAll() {
        return springDataTeamRepository.findAllByOrderByIdAsc()
                .stream()
                .map(TeamJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Team> findVisibleTeamsByPeriodId(
            Long teamPeriodId
    ) {
        return springDataTeamRepository.findAllByTeamPeriodIdAndDeletedAtIsNullOrderByIdAsc(
                        teamPeriodId
                )
                .stream()
                .map(TeamJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<Team> findById(
            Long teamId
    ) {
        return springDataTeamRepository.findById(
                        teamId
                )
                .map(TeamJpaEntity::toDomain);
    }

    @Override
    public Optional<Team> findByIdForUpdate(
            Long teamId
    ) {
        return springDataTeamRepository.findByIdForUpdate(
                        teamId
                )
                .map(TeamJpaEntity::toDomain);
    }

    @Override
    public List<TeamMember> findActiveMembers() {
        return springDataTeamMemberRepository.findActiveMembers()
                .stream()
                .map(this::toTeamMember)
                .toList();
    }

    @Override
    public List<TeamMember> findActiveMembersForUpdate() {
        return springDataTeamMemberRepository.findActiveMembersForUpdate()
                .stream()
                .map(TeamMemberViewJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<TeamMember> findActiveMembersByTeamPeriodIdForUpdate(
            Long teamPeriodId
    ) {
        return springDataTeamMemberRepository.findActiveMembersByTeamPeriodIdForUpdate(
                        teamPeriodId
                )
                .stream()
                .map(TeamMemberViewJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<TeamMember> findActiveMembersByTeamId(
            Long teamId
    ) {
        return springDataTeamMemberRepository.findActiveMembersByTeamId(
                        teamId
                )
                .stream()
                .map(this::toTeamMember)
                .toList();
    }

    @Override
    public List<TeamMember> findActiveMembersByTeamIds(
            List<Long> teamIds
    ) {
        if (teamIds == null
                || teamIds.isEmpty()) {
            return List.of();
        }

        return springDataTeamMemberRepository.findActiveMembersByTeamIds(
                        teamIds
                )
                .stream()
                .map(this::toTeamMember)
                .toList();
    }

    @Override
    public List<TeamMember> findMembersByTeamPeriodIdForList(
            Long teamPeriodId
    ) {
        return springDataTeamMemberRepository.findMembersByTeamPeriodIdForList(
                        teamPeriodId
                )
                .stream()
                .map(this::toTeamMember)
                .toList();
    }

    @Override
    public Optional<TeamMember> findMemberById(
            Long teamMemberId
    ) {
        return springDataTeamMemberRepository.findById(
                        teamMemberId
                )
                .map(TeamMemberViewJpaEntity::toDomain);
    }

    @Override
    public Optional<TeamMember> findMemberByIdForUpdate(
            Long teamMemberId
    ) {
        return springDataTeamMemberRepository.findByIdForUpdate(
                        teamMemberId
                )
                .map(TeamMemberViewJpaEntity::toDomain);
    }

    @Override
    public boolean existsActiveMemberByUserId(
            Long userId
    ) {
        return springDataTeamMemberRepository.existsByUserIdAndLeftAtIsNull(
                userId
        );
    }

    @Override
    public boolean existsByNameAndTeamPeriodId(
            String name,
            Long teamPeriodId
    ) {
        return springDataTeamRepository.existsByNameAndTeamPeriodId(
                name,
                teamPeriodId
        );
    }

    @Override
    public boolean existsByNameAndTeamPeriodIdAndIdNot(
            String name,
            Long teamPeriodId,
            Long teamId
    ) {
        return springDataTeamRepository.existsByNameAndTeamPeriodIdAndIdNot(
                name,
                teamPeriodId,
                teamId
        );
    }

    @Override
    public boolean existsActiveMemberByTeamPeriodId(
            Long teamPeriodId
    ) {
        return springDataTeamMemberRepository.existsActiveMemberByTeamPeriodId(
                teamPeriodId
        );
    }

    @Override
    public void deleteMembersByTeamPeriodId(
            Long teamPeriodId
    ) {
        springDataTeamMemberRepository.deleteByTeamPeriodId(
                teamPeriodId
        );
    }

    @Override
    public void deleteTeamsByTeamPeriodId(
            Long teamPeriodId
    ) {
        springDataTeamRepository.deleteByTeamPeriodId(
                teamPeriodId
        );
    }

    @Override
    public List<UnassignedStudent> findUnassignedStudents(
            Long teamPeriodId
    ) {
        return springDataTeamMemberRepository.findUnassignedStudents(
                        teamPeriodId
                )
                .stream()
                .map(this::toUnassignedStudent)
                .toList();
    }

    @Override
    public List<UserTeamHistoryResult> findUserTeamHistories(
            Long userId
    ) {
        return springDataTeamMemberRepository.findUserTeamHistories(
                        userId
                )
                .stream()
                .map(this::toUserTeamHistoryResult)
                .toList();
    }

    private UserTeamHistoryResult toUserTeamHistoryResult(
            UserTeamHistoryProjection projection
    ) {
        return new UserTeamHistoryResult(
                projection.getTeamId(),
                projection.getTeamName(),
                toStartDate(
                        projection.getJoinedAt()
                ),
                toEndDate(
                        projection.getLeftAt(),
                        projection.getPeriodEndDate()
                )
        );
    }

    private LocalDate toStartDate(
            LocalDateTime joinedAt
    ) {
        if (joinedAt == null) {
            return null;
        }

        return joinedAt.toLocalDate();
    }

    private LocalDate toEndDate(
            LocalDateTime leftAt,
            LocalDate periodEndDate
    ) {
        if (leftAt != null) {
            return leftAt.toLocalDate();
        }

        return periodEndDate;
    }

    private TeamMember toTeamMember(
            TeamMemberProjection projection
    ) {
        return TeamMember.restore(
                projection.getTeamMemberId(),
                projection.getTeamId(),
                projection.getUserId(),
                projection.getUserName(),
                projection.getEmail(),
                projection.getProfileImg(),
                projection.getJoinedAt(),
                projection.getLeftAt()
        );
    }

    private UnassignedStudent toUnassignedStudent(
            UnassignedStudentProjection projection
    ) {
        return UnassignedStudent.restore(
                projection.getUserId(),
                projection.getName(),
                projection.getEmail(),
                projection.getProfileImg()
        );
    }

    private boolean isTeamNameUniqueConstraintViolation(
            DataIntegrityViolationException exception
    ) {
        return isConstraintViolation(
                exception,
                TEAM_NAME_UNIQUE_CONSTRAINT
        );
    }

    private boolean isConstraintViolation(
            DataIntegrityViolationException exception,
            String constraintName
    ) {
        Throwable cause =
                exception.getCause();

        while (cause != null) {
            if (cause instanceof ConstraintViolationException constraintViolationException
                    && constraintName.equalsIgnoreCase(
                    constraintViolationException.getConstraintName()
            )) {
                return true;
            }

            if (cause instanceof SQLException sqlException
                    && sqlException.getMessage() != null
                    && sqlException.getMessage()
                    .contains(constraintName)) {
                return true;
            }

            cause =
                    cause.getCause();
        }

        return false;
    }
}