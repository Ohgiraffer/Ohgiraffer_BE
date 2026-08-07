package com.ohgiraffer.team.presentation.api;

import com.ohgiraffer.security.user.CustomUserPrincipal;
import com.ohgiraffer.team.application.usecase.GetTeamDetailUseCase;
import com.ohgiraffer.team.application.usecase.GetTeamListUseCase;
import com.ohgiraffer.team.application.usecase.GetUnassignedStudentUseCase;
import com.ohgiraffer.team.application.usecase.TeamDetailResult;
import com.ohgiraffer.team.application.usecase.TeamListResult;
import com.ohgiraffer.team.application.usecase.UnassignedStudentResult;
import com.ohgiraffer.team.presentation.api.response.TeamDetailResponse;
import com.ohgiraffer.team.presentation.api.response.TeamListResponse;
import com.ohgiraffer.team.presentation.api.response.UnassignedStudentListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/teams")
public class TeamController {

    private final GetTeamListUseCase getTeamListUseCase;
    private final GetTeamDetailUseCase getTeamDetailUseCase;
    private final GetUnassignedStudentUseCase getUnassignedStudentUseCase;

    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR', 'MANAGER')")
    public ResponseEntity<TeamListResponse> getTeams(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        List<TeamListResult> results =
                getTeamListUseCase.getTeams(
                        principal.getId(),
                        principal.getRole()
                );

        return ResponseEntity.ok(
                TeamListResponse.from(
                        results,
                        principal.getRole()
                )
        );
    }

    @GetMapping("/{teamId:\\d+}")
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR', 'MANAGER')")
    public ResponseEntity<TeamDetailResponse> getTeam(
            @PathVariable Long teamId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        TeamDetailResult result =
                getTeamDetailUseCase.getTeam(
                        teamId,
                        principal.getId(),
                        principal.getRole()
                );

        return ResponseEntity.ok(
                TeamDetailResponse.from(
                        result,
                        principal.getRole()
                )
        );
    }

    @GetMapping("/unassigned-students")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MANAGER')")
    public ResponseEntity<UnassignedStudentListResponse> getUnassignedStudents(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        List<UnassignedStudentResult> results =
                getUnassignedStudentUseCase.getUnassignedStudents(
                        principal.getId(),
                        principal.getRole()
                );

        return ResponseEntity.ok(
                UnassignedStudentListResponse.from(
                        results
                )
        );
    }
}