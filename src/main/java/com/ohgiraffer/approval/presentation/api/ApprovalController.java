package com.ohgiraffer.approval.presentation.api;

import com.ohgiraffer.approval.application.command.CreateLeaveApprovalCommand;
import com.ohgiraffer.approval.application.command.CreatePurchaseApprovalCommand;
import com.ohgiraffer.approval.application.query.ApprovalDetailResult;
import com.ohgiraffer.approval.application.query.ApprovalListItemResult;
import com.ohgiraffer.approval.application.query.ApprovalListScope;
import com.ohgiraffer.approval.application.query.GetApprovalHistoriesQuery;
import com.ohgiraffer.approval.application.result.ApprovalHistoryListResult;
import com.ohgiraffer.approval.application.result.ApprovalPdfResult;
import com.ohgiraffer.approval.application.usecase.*;
import com.ohgiraffer.approval.presentation.api.request.CreateLeaveApprovalRequest;
import com.ohgiraffer.approval.presentation.api.request.CreatePurchaseApprovalRequest;
import com.ohgiraffer.approval.presentation.api.request.RejectApprovalRequest;
import com.ohgiraffer.approval.presentation.api.response.ApprovalDetailResponse;
import com.ohgiraffer.approval.presentation.api.response.ApprovalHistoryListResponse;
import com.ohgiraffer.approval.presentation.api.response.ApprovalListResponse;
import com.ohgiraffer.approval.presentation.api.response.CreateApprovalResponse;
import com.ohgiraffer.security.user.CustomUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/approvals")
public class ApprovalController {

    private final CreateLeaveApprovalUseCase createLeaveApprovalUseCase;
    private final CreatePurchaseApprovalUseCase createPurchaseApprovalUseCase;
    private final GetApprovalListUseCase getApprovalListUseCase;
    private final GetApprovalDetailUseCase getApprovalDetailUseCase;
    private final CheckApprovalUseCase checkApprovalUseCase;
    private final ApproveApprovalUseCase approveApprovalUseCase;
    private final RejectApprovalUseCase rejectApprovalUseCase;
    private final GetApprovalHistoriesUseCase getApprovalHistoriesUseCase;
    private final DownloadApprovalPdfUseCase downloadApprovalPdfUseCase;

    public ApprovalController(
            CreateLeaveApprovalUseCase createLeaveApprovalUseCase,
            CreatePurchaseApprovalUseCase createPurchaseApprovalUseCase,
            GetApprovalListUseCase getApprovalListUseCase,
            GetApprovalDetailUseCase getApprovalDetailUseCase,
            CheckApprovalUseCase checkApprovalUseCase,
            ApproveApprovalUseCase approveApprovalUseCase,
            RejectApprovalUseCase rejectApprovalUseCase,
            GetApprovalHistoriesUseCase getApprovalHistoriesUseCase,
            DownloadApprovalPdfUseCase downloadApprovalPdfUseCase
    ) {
        this.createLeaveApprovalUseCase = createLeaveApprovalUseCase;
        this.createPurchaseApprovalUseCase = createPurchaseApprovalUseCase;
        this.getApprovalListUseCase = getApprovalListUseCase;
        this.getApprovalDetailUseCase = getApprovalDetailUseCase;
        this.checkApprovalUseCase = checkApprovalUseCase;
        this.approveApprovalUseCase = approveApprovalUseCase;
        this.rejectApprovalUseCase = rejectApprovalUseCase;
        this.getApprovalHistoriesUseCase = getApprovalHistoriesUseCase;
        this.downloadApprovalPdfUseCase = downloadApprovalPdfUseCase;
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

    @PatchMapping("/{approvalId}/check")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MANAGER')")
    public ResponseEntity<CreateApprovalResponse> checkApproval(
            @PathVariable Long approvalId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        CreateApprovalResult result =
                checkApprovalUseCase.check(
                        principal.getId(),
                        principal.getRole(),
                        approvalId
                );

        return ResponseEntity.ok(
                CreateApprovalResponse.from(
                        result
                )
        );
    }

    @PatchMapping("/{approvalId}/approve")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MANAGER')")
    public ResponseEntity<CreateApprovalResponse> approveApproval(
            @PathVariable Long approvalId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        CreateApprovalResult result =
                approveApprovalUseCase.approve(
                        principal.getId(),
                        principal.getRole(),
                        approvalId
                );

        return ResponseEntity.ok(
                CreateApprovalResponse.from(
                        result
                )
        );
    }

    @PatchMapping("/{approvalId}/reject")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MANAGER')")
    public ResponseEntity<CreateApprovalResponse> rejectApproval(
            @PathVariable Long approvalId,
            @Valid @RequestBody RejectApprovalRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        CreateApprovalResult result =
                rejectApprovalUseCase.reject(
                        principal.getId(),
                        principal.getRole(),
                        approvalId,
                        request.rejectionReason()
                );

        return ResponseEntity.ok(
                CreateApprovalResponse.from(
                        result
                )
        );
    }

    @GetMapping("/{approvalId}/histories")
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR', 'MANAGER')")
    public ResponseEntity<ApprovalHistoryListResponse> getApprovalHistories(
            @PathVariable Long approvalId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        ApprovalHistoryListResult result =
                getApprovalHistoriesUseCase.getHistories(
                        new GetApprovalHistoriesQuery(
                                approvalId,
                                principal.getId(),
                                principal.getRole()
                        )
                );

        return ResponseEntity.ok(
                ApprovalHistoryListResponse.from(
                        result
                )
        );
    }

    @GetMapping("/{approvalId}/pdf")
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR', 'MANAGER')")
    public ResponseEntity<byte[]> downloadApprovalPdf(
            @PathVariable Long approvalId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        ApprovalPdfResult result =
                downloadApprovalPdfUseCase.downloadPdf(
                        principal.getId(),
                        principal.getRole(),
                        approvalId
                );

        ContentDisposition contentDisposition =
                ContentDisposition.attachment()
                        .filename(
                                result.fileName(),
                                StandardCharsets.UTF_8
                        )
                        .build();

        return ResponseEntity.ok()
                .contentType(
                        MediaType.APPLICATION_PDF
                )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        contentDisposition.toString()
                )
                .body(
                        result.content()
                );
    }
}