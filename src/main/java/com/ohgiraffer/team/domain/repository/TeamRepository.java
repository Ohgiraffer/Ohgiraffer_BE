package com.ohgiraffer.team.domain.repository;

import com.ohgiraffer.team.domain.model.Team;
import com.ohgiraffer.team.domain.model.TeamMember;
import com.ohgiraffer.team.domain.model.UnassignedStudent;

import java.util.List;
import java.util.Optional;

public interface TeamRepository {

    List<Team> findAll();

    Optional<Team> findById(
            Long teamId
    );

    List<TeamMember> findActiveMembersByTeamId(
            Long teamId
    );

    List<TeamMember> findActiveMembersByTeamIds(
            List<Long> teamIds
    );

    boolean existsActiveMember(
            Long teamId,
            Long userId
    );

    List<UnassignedStudent> findUnassignedStudents();
}