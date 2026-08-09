package com.ohgiraffer.team.domain.repository;

import com.ohgiraffer.team.domain.model.Team;
import com.ohgiraffer.team.domain.model.TeamMember;
import com.ohgiraffer.team.domain.model.UnassignedStudent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TeamRepository {

    Team save(
            Team team
    );

    TeamMember saveMember(
            TeamMember teamMember
    );

    List<Team> findAll();

    List<Team> findVisibleTeams();

    Optional<Team> findById(
            Long teamId
    );

    Optional<Team> findByIdForUpdate(
            Long teamId
    );

    List<Team> findArchivableTeamsForUpdate(
            LocalDate today
    );

    List<Team> findDeletableArchivedTeamsForUpdate(
            LocalDateTime deleteThreshold
    );

    List<TeamMember> findActiveMembers();

    List<TeamMember> findActiveMembersForUpdate();

    List<TeamMember> findActiveMembersByTeamId(
            Long teamId
    );

    List<TeamMember> findActiveMembersByTeamIds(
            List<Long> teamIds
    );

    Optional<TeamMember> findMemberById(
            Long teamMemberId
    );

    Optional<TeamMember> findMemberByIdForUpdate(
            Long teamMemberId
    );

    boolean existsActiveMemberByUserId(
            Long userId
    );

    boolean existsByName(
            String name
    );

    boolean existsByNameAndIdNot(
            String name,
            Long teamId
    );

    List<UnassignedStudent> findUnassignedStudents();
}