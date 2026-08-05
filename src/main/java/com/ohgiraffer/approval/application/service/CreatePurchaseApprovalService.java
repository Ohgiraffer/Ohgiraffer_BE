package com.ohgiraffer.approval.application.service;

import com.ohgiraffer.approval.application.command.CreatePurchaseApprovalCommand;
import com.ohgiraffer.approval.application.usecase.CreateApprovalResult;
import com.ohgiraffer.approval.application.usecase.CreatePurchaseApprovalUseCase;
import com.ohgiraffer.approval.domain.model.approval.ApprovalHistory;
import com.ohgiraffer.approval.domain.model.approval.ApprovalPurchaseDetail;
import com.ohgiraffer.approval.domain.model.approval.ApprovalRequest;
import com.ohgiraffer.approval.domain.model.signature.UserSignature;
import com.ohgiraffer.approval.domain.repository.ApprovalHistoryRepository;
import com.ohgiraffer.approval.domain.repository.ApprovalPurchaseDetailRepository;
import com.ohgiraffer.approval.domain.repository.ApprovalRequestRepository;
import com.ohgiraffer.approval.domain.repository.BudgetCategoryRepository;
import com.ohgiraffer.approval.domain.repository.UserSignatureRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.user.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class CreatePurchaseApprovalService
        implements CreatePurchaseApprovalUseCase {

    private final ApprovalRequestRepository approvalRequestRepository;
    private final ApprovalPurchaseDetailRepository approvalPurchaseDetailRepository;
    private final ApprovalHistoryRepository approvalHistoryRepository;
    private final UserSignatureRepository userSignatureRepository;
    private final BudgetCategoryRepository budgetCategoryRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    public CreatePurchaseApprovalService(
            ApprovalRequestRepository approvalRequestRepository,
            ApprovalPurchaseDetailRepository approvalPurchaseDetailRepository,
            ApprovalHistoryRepository approvalHistoryRepository,
            UserSignatureRepository userSignatureRepository,
            BudgetCategoryRepository budgetCategoryRepository,
            UserRepository userRepository,
            Clock clock
    ) {
        this.approvalRequestRepository = approvalRequestRepository;
        this.approvalPurchaseDetailRepository = approvalPurchaseDetailRepository;
        this.approvalHistoryRepository = approvalHistoryRepository;
        this.userSignatureRepository = userSignatureRepository;
        this.budgetCategoryRepository = budgetCategoryRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public CreateApprovalResult create(
            CreatePurchaseApprovalCommand command
    ) {
        LocalDateTime now =
                LocalDateTime.now(
                        clock
                );

        validateCommand(
                command
        );

        validateApproverExists(
                command.approverId()
        );

        validateBudgetCategoryExists(
                command.budgetCategoryId()
        );

        UserSignature userSignature =
                userSignatureRepository
                        .findActiveByUserId(
                                command.requesterId()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.SIGNATURE_NOT_FOUND
                                )
                        );

        ApprovalRequest approvalRequest =
                ApprovalRequest.createPurchase(
                        command.requesterId(),
                        command.approverId(),
                        command.reason().strip(),
                        userSignature.getId(),
                        userSignature.getSignatureImage(),
                        userSignature.getFileType(),
                        now
                );

        ApprovalRequest savedApprovalRequest =
                approvalRequestRepository.save(
                        approvalRequest
                );

        ApprovalPurchaseDetail purchaseDetail =
                ApprovalPurchaseDetail.create(
                        savedApprovalRequest.getId(),
                        command.budgetCategoryId(),
                        command.itemName().strip(),
                        command.amount(),
                        now
                );

        approvalPurchaseDetailRepository.save(
                purchaseDetail
        );

        ApprovalHistory approvalHistory =
                ApprovalHistory.created(
                        savedApprovalRequest.getId(),
                        command.requesterId(),
                        savedApprovalRequest.getStatus(),
                        now
                );

        approvalHistoryRepository.save(
                approvalHistory
        );

        return CreateApprovalResult.from(
                savedApprovalRequest
        );
    }

    private void validateCommand(
            CreatePurchaseApprovalCommand command
    ) {
        if (command == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }

        if (command.requesterId() == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "신청자 아이디는 필수입니다."
            );
        }

        if (command.approverId() == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "결재자 아이디는 필수입니다."
            );
        }

        if (command.budgetCategoryId() == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "예산 카테고리는 필수입니다."
            );
        }

        if (command.itemName() == null || command.itemName().isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "구매 품목명은 필수입니다."
            );
        }

        if (command.reason() == null || command.reason().isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "구매 요청 사유는 필수입니다."
            );
        }

        if (command.amount() == null
                || command.amount().compareTo(
                BigDecimal.ZERO
        ) <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "구매 요청 금액은 0보다 커야 합니다."
            );
        }
    }

    private void validateApproverExists(
            Long approverId
    ) {
        boolean exists =
                userRepository
                        .findById(
                                approverId
                        )
                        .isPresent();

        if (!exists) {
            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND,
                    "결재자를 찾을 수 없습니다."
            );
        }
    }

    private void validateBudgetCategoryExists(
            Long budgetCategoryId
    ) {
        budgetCategoryRepository
                .findById(
                        budgetCategoryId
                )
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.BUDGET_CATEGORY_NOT_FOUND
                        )
                );
    }
}