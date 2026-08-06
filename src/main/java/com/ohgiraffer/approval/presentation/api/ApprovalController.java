package com.ohgiraffer.approval.presentation.api;

import com.ohgiraffer.approval.application.command.CreateLeaveApprovalCommand;
import com.ohgiraffer.approval.application.command.CreatePurchaseApprovalCommand;
import com.ohgiraffer.approval.application.query.ApprovalDetailResult;
import com.ohgiraffer.approval.application.query.ApprovalListItemResult;
import com.ohgiraffer.approval.application.query.ApprovalListScope;
import com.ohgiraffer.approval.application.usecase.CreateApprovalResult;
import com.ohgiraffer.approval.application.usecase.CreateLeaveApprovalUseCase;
import com.ohgiraffer.approval.application.usecase.CreatePurchaseApprovalUseCase;
import com.ohgiraffer.approval.application.usecase.GetApprovalDetailUseCase;
import com.ohgiraffer.approval.application.usecase.GetApprovalListUseCase;
import com.ohgiraffer.approval.presentation.api.request.CreateLeaveApprovalRequest;
import com.ohgiraffer.approval.presentation.api.request.CreatePurchaseApprovalRequest;
import com.ohgiraffer.approval.presentation.api.response.ApprovalDetailResponse;
import com.ohgiraffer.approval.presentation.api.response.ApprovalListResponse;
import com.ohgiraffer.approval.presentation.api.response.CreateApprovalResponse;
import com.ohgiraffer.security.user.CustomUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/approvals")
public class ApprovalController {

    private final CreateLeaveApprovalUseCase createLeaveApprovalUseCase;
    private final CreatePurchaseApprovalUseCase createPurchaseApprovalUseCase;
    private final GetApprovalListUseCase getApprovalListUseCase;
    private final GetApprovalDetailUseCase getApprovalDetailUseCase;

    public ApprovalController(
            CreateLeaveApprovalUseCase createLeaveApprovalUseCase,
            CreatePurchaseApprovalUseCase createPurchaseApprovalUseCase,
            GetApprovalListUseCase getApprovalListUseCase,
            GetApprovalDetailUseCase getApprovalDetailUseCase
    ) {
        this.createLeaveApprovalUseCase = createLeaveApprovalUseCase;
        this.createPurchaseApprovalUseCase = createPurchaseApprovalUseCase;
        this.getApprovalListUseCase = getApprovalListUseCase;
        this.getApprovalDetailUseCase = getApprovalDetailUseCase;
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
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MANAGER')")
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

    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR', 'MANAGER')")
    public ResponseEntity<ApprovalListResponse> getApprovals(
            @RequestParam ApprovalListScope scope,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        List<ApprovalListItemResult> results =
                getApprovalListUseCase.getApprovals(
                        principal.getId(),
                        principal.getRole(),
                        scope
                );

        return ResponseEntity.ok(
                ApprovalListResponse.from(
                        results
                )
        );
    }

    @GetMapping("/{approvalId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR', 'MANAGER')")
    public ResponseEntity<ApprovalDetailResponse> getApprovalDetail(
            @PathVariable Long approvalId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        ApprovalDetailResult result =
                getApprovalDetailUseCase.getApprovalDetail(
                        principal.getId(),
                        principal.getRole(),
                        approvalId
                );

        return ResponseEntity.ok(
                ApprovalDetailResponse.from(
                        result
                )
        );
    }
}