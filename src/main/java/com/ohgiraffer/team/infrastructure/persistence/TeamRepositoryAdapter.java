package com.ohgiraffer.team.infrastructure.persistence;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.team.domain.model.Team;
import com.ohgiraffer.team.domain.model.TeamMember;
import com.ohgiraffer.team.domain.model.UnassignedStudent;
import com.ohgiraffer.team.domain.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TeamRepositoryAdapter
        implements TeamRepository {

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
            if (isTeamNameUniqueConstraintViolation(exception)) {
                throw new BusinessException(
                        ErrorCode.TEAM_DUPLICATE_NAME,
                        exception
                );
            }

            throw exception;
        }
    }

    @Override
    public TeamMember saveMember(
            TeamMember teamMember
    ) {
        try {
            TeamMemberViewJpaEntity savedEntity =
                    springDataTeamMemberRepository.saveAndFlush(
                            TeamMemberViewJpaEntity.from(
                                    teamMember
                            )
                    );

            return savedEntity.toDomain();
        } catch (DataIntegrityViolationException exception) {
            if (isActiveTeamMemberUniqueConstraintViolation(exception)) {
                throw new BusinessException(
                        ErrorCode.TEAM_MEMBER_ALREADY_ASSIGNED,
                        exception
                );
            }

            throw exception;
        }
    }

    @Override
    public List<Team> findAll() {
        return springDataTeamRepository.findAllByOrderByIdAsc()
                .stream()
                .map(TeamJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Team> findVisibleTeams() {
        return springDataTeamRepository.findAllByArchivedAtIsNullAndDeletedAtIsNullOrderByIdAsc()
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
    public List<Team> findArchivableTeamsForUpdate(
            LocalDate today
    ) {
        return springDataTeamRepository.findArchivableTeamsForUpdate(
                        today
                )
                .stream()
                .map(TeamJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Team> findDeletableArchivedTeamsForUpdate(
            LocalDateTime deleteThreshold
    ) {
        return springDataTeamRepository.findDeletableArchivedTeamsForUpdate(
                        deleteThreshold
                )
                .stream()
                .map(TeamJpaEntity::toDomain)
                .toList();
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
    public boolean existsByName(
            String name
    ) {
        return springDataTeamRepository.existsByName(
                name
        );
    }

    @Override
    public boolean existsByNameAndIdNot(
            String name,
            Long teamId
    ) {
        return springDataTeamRepository.existsByNameAndIdNot(
                name,
                teamId
        );
    }

    @Override
    public List<UnassignedStudent> findUnassignedStudents() {
        return springDataTeamMemberRepository.findUnassignedStudents()
                .stream()
                .map(this::toUnassignedStudent)
                .toList();
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
                projection.getEmail()
        );
    }

    private boolean isTeamNameUniqueConstraintViolation(
            DataIntegrityViolationException exception
    ) {
        Throwable current =
                exception;

        while (current != null) {
            String message =
                    current.getMessage();

            if (message != null
                    && message.toLowerCase()
                    .contains(
                            "uq_team_name"
                    )) {
                return true;
            }

            current =
                    current.getCause();
        }

        return false;
    }

    private boolean isActiveTeamMemberUniqueConstraintViolation(
            DataIntegrityViolationException exception
    ) {
        Throwable current =
                exception;

        while (current != null) {
            String message =
                    current.getMessage();

            if (message != null
                    && message.toLowerCase()
                    .contains(
                            "uq_team_member_active_user"
                    )) {
                return true;
            }

            current =
                    current.getCause();
        }

        return false;
    }
}