package com.ohgiraffer.team.domain.repository;

import com.ohgiraffer.team.application.usecase.UserTeamHistoryResult;
import com.ohgiraffer.team.domain.model.Team;
import com.ohgiraffer.team.domain.model.TeamMember;
import com.ohgiraffer.team.domain.model.UnassignedStudent;

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

    List<Team> findVisibleTeamsByPeriodId(
            Long teamPeriodId
    );

    Optional<Team> findById(
            Long teamId
    );

    Optional<Team> findByIdForUpdate(
            Long teamId
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

    boolean existsByNameAndTeamPeriodId(
            String name,
            Long teamPeriodId
    );

    boolean existsByNameAndTeamPeriodIdAndIdNot(
            String name,
            Long teamPeriodId,
            Long teamId
    );

    boolean existsActiveMemberByTeamPeriodId(
            Long teamPeriodId
    );

    void deleteMembersByTeamPeriodId(
            Long teamPeriodId
    );

    void deleteTeamsByTeamPeriodId(
            Long teamPeriodId
    );

    List<UnassignedStudent> findUnassignedStudents();

    List<UserTeamHistoryResult> findUserTeamHistories(
            Long userId
    );
}