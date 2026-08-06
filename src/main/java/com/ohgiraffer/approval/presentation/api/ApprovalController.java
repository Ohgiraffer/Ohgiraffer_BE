package com.ohgiraffer.approval.presentation.api;

import com.ohgiraffer.approval.application.command.CreateLeaveApprovalCommand;
import com.ohgiraffer.approval.application.command.CreatePurchaseApprovalCommand;
import com.ohgiraffer.approval.application.usecase.CreateApprovalResult;
import com.ohgiraffer.approval.application.usecase.CreateLeaveApprovalUseCase;
import com.ohgiraffer.approval.application.usecase.CreatePurchaseApprovalUseCase;
import com.ohgiraffer.approval.presentation.api.request.CreateLeaveApprovalRequest;
import com.ohgiraffer.approval.presentation.api.request.CreatePurchaseApprovalRequest;
import com.ohgiraffer.approval.presentation.api.response.CreateApprovalResponse;
import com.ohgiraffer.security.user.CustomUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/approvals")
public class ApprovalController {

    private final CreateLeaveApprovalUseCase createLeaveApprovalUseCase;
    private final CreatePurchaseApprovalUseCase createPurchaseApprovalUseCase;

    public ApprovalController(
            CreateLeaveApprovalUseCase createLeaveApprovalUseCase,
            CreatePurchaseApprovalUseCase createPurchaseApprovalUseCase
    ) {
        this.createLeaveApprovalUseCase = createLeaveApprovalUseCase;
        this.createPurchaseApprovalUseCase = createPurchaseApprovalUseCase;
    }

    @PostMapping("/leave")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<CreateApprovalResponse> createLeaveApproval(
            @Valid @RequestBody CreateLeaveApprovalRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        CreateLeaveApprovalCommand command =
                new CreateLeaveApprovalCommand(
                        principal.getId(),
                        request.approverId(),
                        request.startDate(),
                        request.endDate()
                );

        CreateApprovalResult result =
                createLeaveApprovalUseCase.create(
                        command
                );

        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        CreateApprovalResponse.from(
                                result
                        )
                );
    }

    @PostMapping("/purchases")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER')")
    public ResponseEntity<CreateApprovalResponse> createPurchaseApproval(
            @Valid @RequestBody CreatePurchaseApprovalRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        CreatePurchaseApprovalCommand command =
                new CreatePurchaseApprovalCommand(
                        principal.getId(),
                        request.approverId(),
                        request.budgetCategoryId(),
                        request.itemName(),
                        request.amount(),
                        request.reason()
                );

        CreateApprovalResult result =
                createPurchaseApprovalUseCase.create(
                        command
                );

        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        CreateApprovalResponse.from(
                                result
                        )
                );
    }
}