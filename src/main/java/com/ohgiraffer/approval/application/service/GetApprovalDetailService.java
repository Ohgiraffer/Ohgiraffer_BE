package com.ohgiraffer.approval.application.service;

import com.ohgiraffer.approval.application.query.ApprovalDetailResult;
import com.ohgiraffer.approval.application.usecase.GetApprovalDetailUseCase;
import com.ohgiraffer.approval.domain.model.approval.ApprovalLeaveDetail;
import com.ohgiraffer.approval.domain.model.approval.ApprovalPurchaseDetail;
import com.ohgiraffer.approval.domain.model.approval.ApprovalRequest;
import com.ohgiraffer.approval.domain.model.approval.ApprovalStatus;
import com.ohgiraffer.approval.domain.model.approval.ApprovalType;
import com.ohgiraffer.approval.domain.model.budget.BudgetCategory;
import com.ohgiraffer.approval.domain.repository.ApprovalLeaveDetailRepository;
import com.ohgiraffer.approval.domain.repository.ApprovalPurchaseDetailRepository;
import com.ohgiraffer.approval.domain.repository.ApprovalRequestRepository;
import com.ohgiraffer.approval.domain.repository.BudgetCategoryRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.user.domain.model.User;
import com.ohgiraffer.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
@RequiredArgsConstructor
public class GetApprovalDetailService implements GetApprovalDetailUseCase {

    private final ApprovalRequestRepository approvalRequestRepository;
    private final ApprovalLeaveDetailRepository approvalLeaveDetailRepository;
    private final ApprovalPurchaseDetailRepository approvalPurchaseDetailRepository;
    private final BudgetCategoryRepository budgetCategoryRepository;
    private final UserRepository userRepository;

    @Override
    public ApprovalDetailResult getApprovalDetail(
            Long loginUserId,
            Role loginUserRole,
            Long approvalId
    ) {
        validateRequest(
                loginUserId,
                loginUserRole,
                approvalId
        );

        ApprovalRequest approvalRequest = approvalRequestRepository.findById(
                        approvalId
                )
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.APPROVAL_NOT_FOUND
                ));

        validateAccess(
                loginUserId,
                loginUserRole,
                approvalRequest
        );

        ApprovalLeaveDetail leaveDetail = null;
        ApprovalPurchaseDetail purchaseDetail = null;
        BudgetCategory budgetCategory = null;

        if (approvalRequest.getRequestType() == ApprovalType.LEAVE) {
            leaveDetail = approvalLeaveDetailRepository.findByApprovalId(
                            approvalRequest.getId()
                    )
                    .orElse(null);
        }

        if (approvalRequest.getRequestType() == ApprovalType.PURCHASE) {
            purchaseDetail = approvalPurchaseDetailRepository.findByApprovalId(
                            approvalRequest.getId()
                    )
                    .orElse(null);

            if (purchaseDetail != null) {
                budgetCategory = budgetCategoryRepository.findById(
                                purchaseDetail.getBudgetCategoryId()
                        )
                        .orElse(null);
            }
        }

        return new ApprovalDetailResult(
                approvalRequest.getId(),
                approvalRequest.getRequestType(),
                approvalRequest.getStatus(),
                approvalRequest.getTitle(),
                approvalRequest.getReason(),
                approvalRequest.getRejectionReason(),
                approvalRequest.getRequesterId(),
                findUserName(
                        approvalRequest.getRequesterId()
                ),
                approvalRequest.getApproverId(),
                findUserNameOrNull(
                        approvalRequest.getApproverId()
                ),
                approvalRequest.getRequestedAt(),
                approvalRequest.getConfirmedAt(),
                approvalRequest.getProcessedAt(),
                leaveDetail == null ? null : leaveDetail.getStartDate(),
                leaveDetail == null ? null : leaveDetail.getEndDate(),
                purchaseDetail == null ? null : purchaseDetail.getBudgetCategoryId(),
                budgetCategory == null ? null : budgetCategory.getName(),
                purchaseDetail == null ? null : purchaseDetail.getItemName(),
                purchaseDetail == null ? null : purchaseDetail.getAmount(),
                toSignatureDataUri(
                        approvalRequest
                )
        );
    }

    private void validateRequest(
            Long loginUserId,
            Role loginUserRole,
            Long approvalId
    ) {
        if (loginUserId == null || loginUserRole == null || approvalId == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }
    }

    private void validateAccess(
            Long loginUserId,
            Role loginUserRole,
            ApprovalRequest approvalRequest
    ) {
        if (approvalRequest.getRequesterId().equals(
                loginUserId
        )) {
            return;
        }

        if (approvalRequest.getApproverId() != null
                && approvalRequest.getApproverId().equals(
                loginUserId
        )) {
            return;
        }

        if (approvalRequest.getStatus() == ApprovalStatus.PENDING
                && canProcessApproval(
                loginUserRole
        )
                && isSameBootcamp(
                loginUserId,
                approvalRequest.getRequesterId()
        )) {
            return;
        }

        throw new BusinessException(
                ErrorCode.APPROVAL_ACCESS_DENIED
        );
    }

    private boolean canProcessApproval(
            Role role
    ) {
        return role == Role.INSTRUCTOR || role == Role.MANAGER;
    }

    private boolean isSameBootcamp(
            Long loginUserId,
            Long requesterId
    ) {
        Long loginUserBootcampId = findBootcampId(
                loginUserId
        );

        Long requesterBootcampId = findBootcampId(
                requesterId
        );

        return loginUserBootcampId.equals(
                requesterBootcampId
        );
    }

    private Long findBootcampId(
            Long userId
    ) {
        return userRepository.findBootcampIdByUserId(
                        userId
                )
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.USER_NOT_FOUND
                ));
    }

    private String findUserName(
            Long userId
    ) {
        return userRepository
                .findById(
                        userId
                )
                .map(
                        User::getName
                )
                .orElse(
                        ""
                );
    }

    private String findUserNameOrNull(
            Long userId
    ) {
        if (userId == null) {
            return null;
        }

        return findUserName(
                userId
        );
    }

    private String toSignatureDataUri(
            ApprovalRequest approvalRequest
    ) {
        byte[] signatureImage = approvalRequest.getSignatureImageSnapshot();

        if (signatureImage == null || approvalRequest.getSignatureFileTypeSnapshot() == null) {
            return null;
        }

        return "data:"
                + approvalRequest.getSignatureFileTypeSnapshot()
                + ";base64,"
                + Base64.getEncoder().encodeToString(
                signatureImage
        );
    }
}