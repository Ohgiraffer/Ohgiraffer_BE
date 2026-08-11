package com.ohgiraffer.team.presentation.api;

import com.ohgiraffer.security.user.CustomUserPrincipal;
import com.ohgiraffer.team.application.command.CreateTeamPeriodCommand;
import com.ohgiraffer.team.application.command.SaveTeamConfigurationCommand;
import com.ohgiraffer.team.application.command.UpdateTeamPeriodCommand;
import com.ohgiraffer.team.application.usecase.CreateTeamPeriodUseCase;
import com.ohgiraffer.team.application.usecase.GetTeamHistoryUseCase;
import com.ohgiraffer.team.application.usecase.GetTeamListUseCase;
import com.ohgiraffer.team.application.usecase.GetTeamPeriodListUseCase;
import com.ohgiraffer.team.application.usecase.GetUnassignedStudentUseCase;
import com.ohgiraffer.team.application.usecase.SaveTeamConfigurationUseCase;
import com.ohgiraffer.team.application.usecase.TeamHistoryResult;
import com.ohgiraffer.team.application.usecase.TeamListResult;
import com.ohgiraffer.team.application.usecase.TeamPeriodResult;
import com.ohgiraffer.team.application.usecase.UnassignedStudentResult;
import com.ohgiraffer.team.application.usecase.UpdateTeamPeriodUseCase;
import com.ohgiraffer.team.presentation.api.request.CreateTeamPeriodRequest;
import com.ohgiraffer.team.presentation.api.request.SaveTeamConfigurationRequest;
import com.ohgiraffer.team.presentation.api.request.UpdateTeamPeriodRequest;
import com.ohgiraffer.team.presentation.api.response.CreateTeamPeriodResponse;
import com.ohgiraffer.team.presentation.api.response.TeamHistoryResponse;
import com.ohgiraffer.team.presentation.api.response.TeamListResponse;
import com.ohgiraffer.team.presentation.api.response.TeamPeriodListResponse;
import com.ohgiraffer.team.presentation.api.response.UnassignedStudentListResponse;
import com.ohgiraffer.team.presentation.api.response.UpdateTeamPeriodResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/teams")
public class TeamController {

    private final GetTeamListUseCase getTeamListUseCase;
    private final GetUnassignedStudentUseCase getUnassignedStudentUseCase;
    private final GetTeamHistoryUseCase getTeamHistoryUseCase;
    private final GetTeamPeriodListUseCase getTeamPeriodListUseCase;
    private final CreateTeamPeriodUseCase createTeamPeriodUseCase;
    private final UpdateTeamPeriodUseCase updateTeamPeriodUseCase;
    private final SaveTeamConfigurationUseCase saveTeamConfigurationUseCase;

    @GetMapping("/periods")
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR', 'MANAGER')")
    public ResponseEntity<TeamPeriodListResponse> getTeamPeriods(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        List<TeamPeriodResult> results =
                getTeamPeriodListUseCase.getTeamPeriods(
                        principal.getId(),
                        principal.getRole()
                );

        return ResponseEntity.ok(
                TeamPeriodListResponse.from(
                        results
                )
        );
    }

    @PostMapping("/periods")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MANAGER')")
    public ResponseEntity<CreateTeamPeriodResponse> createTeamPeriod(
            @Valid @RequestBody CreateTeamPeriodRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        CreateTeamPeriodCommand command =
                request.toCommand(
                        principal.getId()
                );

        TeamPeriodResult result =
                createTeamPeriodUseCase.createTeamPeriod(
                        command,
                        principal.getRole()
                );

        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        CreateTeamPeriodResponse.from(
                                result
                        )
                );
    }

    @PatchMapping("/periods/{periodId:\\d+}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MANAGER')")
    public ResponseEntity<UpdateTeamPeriodResponse> updateTeamPeriod(
            @PathVariable Long periodId,
            @Valid @RequestBody UpdateTeamPeriodRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        UpdateTeamPeriodCommand command =
                request.toCommand(
                        principal.getId(),
                        periodId
                );

        TeamPeriodResult result =
                updateTeamPeriodUseCase.updateTeamPeriod(
                        command,
                        principal.getRole()
                );

        return ResponseEntity.ok(
                UpdateTeamPeriodResponse.from(
                        result
                )
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR', 'MANAGER')")
    public ResponseEntity<TeamListResponse> getTeams(
            @RequestParam Long periodId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        List<TeamListResult> results =
                getTeamListUseCase.getTeams(
                        principal.getId(),
                        principal.getRole(),
                        periodId
                );

        return ResponseEntity.ok(
                TeamListResponse.from(
                        results,
                        principal.getRole()
                )
        );
    }

    @GetMapping("/histories")
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR', 'MANAGER')")
    public ResponseEntity<TeamHistoryResponse> getTeamHistories(
            @RequestParam Long periodId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        TeamHistoryResult result =
                getTeamHistoryUseCase.getTeamHistories(
                        principal.getId(),
                        principal.getRole(),
                        periodId,
                        startDate,
                        endDate
                );

        return ResponseEntity.ok(
                TeamHistoryResponse.from(
                        result
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

    @PatchMapping("/configuration")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MANAGER')")
    public ResponseEntity<Void> saveTeamConfiguration(
            @Valid @RequestBody SaveTeamConfigurationRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        SaveTeamConfigurationCommand command =
                request.toCommand(
                        principal.getId()
                );

        saveTeamConfigurationUseCase.saveTeamConfiguration(
                command,
                principal.getRole()
        );

        return ResponseEntity.noContent()
                .build();
    }
}