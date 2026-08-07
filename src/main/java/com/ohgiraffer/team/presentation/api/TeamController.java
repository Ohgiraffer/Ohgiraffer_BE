package com.ohgiraffer.team.presentation.api;

import com.ohgiraffer.security.user.CustomUserPrincipal;
import com.ohgiraffer.team.application.command.AssignTeamMemberCommand;
import com.ohgiraffer.team.application.command.CreateTeamCommand;
import com.ohgiraffer.team.application.command.MoveTeamMemberCommand;
import com.ohgiraffer.team.application.command.RemoveTeamMemberCommand;
import com.ohgiraffer.team.application.command.UpdateTeamCommand;
import com.ohgiraffer.team.application.usecase.AssignTeamMemberResult;
import com.ohgiraffer.team.application.usecase.AssignTeamMemberUseCase;
import com.ohgiraffer.team.application.usecase.CreateTeamResult;
import com.ohgiraffer.team.application.usecase.CreateTeamUseCase;
import com.ohgiraffer.team.application.usecase.GetTeamDetailUseCase;
import com.ohgiraffer.team.application.usecase.GetTeamListUseCase;
import com.ohgiraffer.team.application.usecase.GetUnassignedStudentUseCase;
import com.ohgiraffer.team.application.usecase.MoveTeamMemberUseCase;
import com.ohgiraffer.team.application.usecase.RemoveTeamMemberUseCase;
import com.ohgiraffer.team.application.usecase.TeamDetailResult;
import com.ohgiraffer.team.application.usecase.TeamListResult;
import com.ohgiraffer.team.application.usecase.UnassignedStudentResult;
import com.ohgiraffer.team.application.usecase.UpdateTeamUseCase;
import com.ohgiraffer.team.presentation.api.request.AssignTeamMemberRequest;
import com.ohgiraffer.team.presentation.api.request.CreateTeamRequest;
import com.ohgiraffer.team.presentation.api.request.MoveTeamMemberRequest;
import com.ohgiraffer.team.presentation.api.request.UpdateTeamRequest;
import com.ohgiraffer.team.presentation.api.response.AssignTeamMemberResponse;
import com.ohgiraffer.team.presentation.api.response.CreateTeamResponse;
import com.ohgiraffer.team.presentation.api.response.TeamDetailResponse;
import com.ohgiraffer.team.presentation.api.response.TeamListResponse;
import com.ohgiraffer.team.presentation.api.response.UnassignedStudentListResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    private final CreateTeamUseCase createTeamUseCase;
    private final UpdateTeamUseCase updateTeamUseCase;
    private final AssignTeamMemberUseCase assignTeamMemberUseCase;
    private final MoveTeamMemberUseCase moveTeamMemberUseCase;
    private final RemoveTeamMemberUseCase removeTeamMemberUseCase;

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

    @PostMapping
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MANAGER')")
    public ResponseEntity<CreateTeamResponse> createTeam(
            @Valid @RequestBody CreateTeamRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        CreateTeamCommand command =
                request.toCommand(
                        principal.getId()
                );

        CreateTeamResult result =
                createTeamUseCase.createTeam(
                        command,
                        principal.getRole()
                );

        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        CreateTeamResponse.from(
                                result
                        )
                );
    }

    @PatchMapping("/{teamId:\\d+}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MANAGER')")
    public ResponseEntity<TeamDetailResponse> updateTeam(
            @PathVariable Long teamId,
            @Valid @RequestBody UpdateTeamRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        UpdateTeamCommand command =
                request.toCommand(
                        teamId,
                        principal.getId()
                );

        TeamDetailResult result =
                updateTeamUseCase.updateTeam(
                        command,
                        principal.getRole()
                );

        return ResponseEntity.ok(
                TeamDetailResponse.from(
                        result,
                        principal.getRole()
                )
        );
    }

    @PostMapping("/{teamId:\\d+}/members")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MANAGER')")
    public ResponseEntity<AssignTeamMemberResponse> assignTeamMember(
            @PathVariable Long teamId,
            @Valid @RequestBody AssignTeamMemberRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        AssignTeamMemberCommand command =
                request.toCommand(
                        teamId,
                        principal.getId()
                );

        AssignTeamMemberResult result =
                assignTeamMemberUseCase.assignTeamMember(
                        command,
                        principal.getRole()
                );

        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        AssignTeamMemberResponse.from(
                                result
                        )
                );
    }

    @PatchMapping("/{teamId:\\d+}/members/{memberId:\\d+}/move")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MANAGER')")
    public ResponseEntity<AssignTeamMemberResponse> moveTeamMember(
            @PathVariable Long teamId,
            @PathVariable Long memberId,
            @Valid @RequestBody MoveTeamMemberRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        MoveTeamMemberCommand command =
                request.toCommand(
                        teamId,
                        memberId,
                        principal.getId()
                );

        AssignTeamMemberResult result =
                moveTeamMemberUseCase.moveTeamMember(
                        command,
                        principal.getRole()
                );

        return ResponseEntity.ok(
                AssignTeamMemberResponse.from(
                        result
                )
        );
    }

    @DeleteMapping("/{teamId:\\d+}/members/{memberId:\\d+}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MANAGER')")
    public ResponseEntity<Void> removeTeamMember(
            @PathVariable Long teamId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        RemoveTeamMemberCommand command =
                new RemoveTeamMemberCommand(
                        teamId,
                        memberId,
                        principal.getId()
                );

        removeTeamMemberUseCase.removeTeamMember(
                command,
                principal.getRole()
        );

        return ResponseEntity.noContent()
                .build();
    }
}