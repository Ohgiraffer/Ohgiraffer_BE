package com.ohgiraffer.approval.presentation.api;

import com.ohgiraffer.approval.application.query.TraineeApprovalHistoryResult;
import com.ohgiraffer.approval.application.usecase.GetTraineeApprovalHistoryUseCase;
import com.ohgiraffer.approval.presentation.api.response.TraineeApprovalHistoryResponse;
import com.ohgiraffer.security.user.CustomUserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/trainees/{traineeId}/approvals")
public class TraineeApprovalController {

    private final GetTraineeApprovalHistoryUseCase getTraineeApprovalHistoryUseCase;

    public TraineeApprovalController(
            GetTraineeApprovalHistoryUseCase getTraineeApprovalHistoryUseCase
    ) {
        this.getTraineeApprovalHistoryUseCase = getTraineeApprovalHistoryUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MANAGER')")
    public ResponseEntity<TraineeApprovalHistoryResponse> getTraineeApprovalHistory(
            @PathVariable Long traineeId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        TraineeApprovalHistoryResult result =
                getTraineeApprovalHistoryUseCase.getApprovalHistory(
                        principal.getId(),
                        principal.getRole(),
                        traineeId
                );

        return ResponseEntity.ok(
                TraineeApprovalHistoryResponse.from(
                        result
                )
        );
    }
}