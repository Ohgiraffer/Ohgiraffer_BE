package com.ohgiraffer.team.infrastructure.persistence;

import com.ohgiraffer.team.domain.model.Team;
import com.ohgiraffer.team.domain.model.TeamMember;
import com.ohgiraffer.team.domain.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TeamRepositoryAdapter
        implements TeamRepository {

    private final SpringDataTeamRepository springDataTeamRepository;
    private final SpringDataTeamMemberRepository springDataTeamMemberRepository;

    @Override
    public List<Team> findAll() {
        return springDataTeamRepository.findAll()
                .stream()
                .map(TeamJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<Team> findById(
            Long teamId
    ) {
        return springDataTeamRepository.findById(teamId)
                .map(TeamJpaEntity::toDomain);
    }

    @Override
    public List<TeamMember> findActiveMembersByTeamId(
            Long teamId
    ) {
        return springDataTeamMemberRepository
                .findActiveMembersByTeamId(teamId)
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

        return springDataTeamMemberRepository
                .findActiveMembersByTeamIds(teamIds)
                .stream()
                .map(this::toTeamMember)
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
}