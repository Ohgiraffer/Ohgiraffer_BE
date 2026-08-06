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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class CreatePurchaseApprovalService
        implements CreatePurchaseApprovalUseCase {

    private static final int MAX_ITEM_NAME_LENGTH = 100;
    private static final int MAX_REASON_LENGTH = 1000;
    private static final int MAX_AMOUNT_INTEGER_DIGITS = 12;
    private static final int MAX_AMOUNT_FRACTION_DIGITS = 2;
    private static final BigDecimal MAX_AMOUNT = new BigDecimal(
            "999999999999.99"
    );

    private final ApprovalRequestRepository approvalRequestRepository;
    private final ApprovalPurchaseDetailRepository approvalPurchaseDetailRepository;
    private final ApprovalHistoryRepository approvalHistoryRepository;
    private final UserSignatureRepository userSignatureRepository;
    private final BudgetCategoryRepository budgetCategoryRepository;
    private final Clock clock;

    public CreatePurchaseApprovalService(
            ApprovalRequestRepository approvalRequestRepository,
            ApprovalPurchaseDetailRepository approvalPurchaseDetailRepository,
            ApprovalHistoryRepository approvalHistoryRepository,
            UserSignatureRepository userSignatureRepository,
            BudgetCategoryRepository budgetCategoryRepository,
            Clock clock
    ) {
        this.approvalRequestRepository = approvalRequestRepository;
        this.approvalPurchaseDetailRepository = approvalPurchaseDetailRepository;
        this.approvalHistoryRepository = approvalHistoryRepository;
        this.userSignatureRepository = userSignatureRepository;
        this.budgetCategoryRepository = budgetCategoryRepository;
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

        validateRequesterId(
                command.requesterId()
        );

        validateBudgetCategoryId(
                command.budgetCategoryId()
        );

        validateItemName(
                command.itemName()
        );

        validateReason(
                command.reason()
        );

        validateAmount(
                command.amount()
        );
    }

    private void validateRequesterId(
            Long requesterId
    ) {
        if (requesterId == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "신청자 아이디는 필수입니다."
            );
        }
    }

    private void validateBudgetCategoryId(
            Long budgetCategoryId
    ) {
        if (budgetCategoryId == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "예산 카테고리는 필수입니다."
            );
        }
    }

    private void validateItemName(
            String itemName
    ) {
        if (itemName == null || itemName.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "구매 품목명은 필수입니다."
            );
        }

        if (itemName.strip().length() > MAX_ITEM_NAME_LENGTH) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "구매 품목명은 100자를 초과할 수 없습니다."
            );
        }
    }

    private void validateReason(
            String reason
    ) {
        if (reason == null || reason.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "구매 요청 사유는 필수입니다."
            );
        }

        if (reason.strip().length() > MAX_REASON_LENGTH) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "구매 요청 사유는 1000자를 초과할 수 없습니다."
            );
        }
    }

    private void validateAmount(
            BigDecimal amount
    ) {
        if (amount == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "구매 요청 금액은 필수입니다."
            );
        }

        if (amount.compareTo(
                BigDecimal.ZERO
        ) <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "구매 요청 금액은 0보다 커야 합니다."
            );
        }

        if (amount.compareTo(
                MAX_AMOUNT
        ) > 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "구매 요청 금액이 허용 범위를 초과했습니다."
            );
        }

        if (amount.scale() > MAX_AMOUNT_FRACTION_DIGITS) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "구매 요청 금액은 소수점 이하 2자리까지만 입력할 수 있습니다."
            );
        }

        if (amount.precision() - amount.scale() > MAX_AMOUNT_INTEGER_DIGITS) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "구매 요청 금액의 정수부가 허용 범위를 초과했습니다."
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