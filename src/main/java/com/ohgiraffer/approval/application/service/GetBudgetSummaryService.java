package com.ohgiraffer.approval.application.service;

import com.ohgiraffer.approval.application.query.BudgetCategorySummaryResult;
import com.ohgiraffer.approval.application.query.BudgetSummaryResult;
import com.ohgiraffer.approval.application.usecase.GetBudgetSummaryUseCase;
import com.ohgiraffer.approval.domain.model.budget.BudgetAllocation;
import com.ohgiraffer.approval.domain.model.budget.BudgetCategory;
import com.ohgiraffer.approval.domain.model.budget.ExternalSheetLink;
import com.ohgiraffer.approval.domain.repository.BudgetAllocationRepository;
import com.ohgiraffer.approval.domain.repository.BudgetCategoryRepository;
import com.ohgiraffer.approval.domain.repository.ExternalSheetLinkRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetBudgetSummaryService implements GetBudgetSummaryUseCase {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(
            100
    );

    private final ExternalSheetLinkRepository externalSheetLinkRepository;
    private final BudgetAllocationRepository budgetAllocationRepository;
    private final BudgetCategoryRepository budgetCategoryRepository;

    @Override
    public BudgetSummaryResult getSummary() {
        ExternalSheetLink externalSheetLink = externalSheetLinkRepository.findByDomain(
                        ExternalSheetLink.budgetDomain()
                )
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INVALID_INPUT_VALUE
                ));

        List<BudgetAllocation> allocations = budgetAllocationRepository.findAll();

        Map<Long, BudgetCategory> categoryById = budgetCategoryRepository.findAll()
                .stream()
                .collect(
                        Collectors.toMap(
                                BudgetCategory::getId,
                                category -> category
                        )
                );

        List<BudgetCategorySummaryResult> categories = allocations.stream()
                .filter(
                        allocation -> categoryById.containsKey(
                                allocation.getBudgetCategoryId()
                        )
                )
                .sorted(
                        Comparator.comparing(
                                allocation -> categoryById.get(
                                        allocation.getBudgetCategoryId()
                                ).getName()
                        )
                )
                .map(
                        allocation -> toCategorySummary(
                                allocation,
                                categoryById.get(
                                        allocation.getBudgetCategoryId()
                                )
                        )
                )
                .toList();

        BigDecimal totalBudgetAmount = categories.stream()
                .map(
                        BudgetCategorySummaryResult::totalAmount
                )
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        BigDecimal usedAmount = categories.stream()
                .map(
                        BudgetCategorySummaryResult::usedAmount
                )
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        BigDecimal remainingAmount = categories.stream()
                .map(
                        BudgetCategorySummaryResult::remainingAmount
                )
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        LocalDateTime lastSyncedAt = allocations.stream()
                .map(
                        BudgetAllocation::getLastSyncedAt
                )
                .filter(
                        syncedAt -> syncedAt != null
                )
                .max(
                        LocalDateTime::compareTo
                )
                .orElse(
                        externalSheetLink.getLastSyncedAt()
                );

        return new BudgetSummaryResult(
                totalBudgetAmount,
                usedAmount,
                remainingAmount,
                calculateUsageRate(
                        usedAmount,
                        totalBudgetAmount
                ),
                lastSyncedAt,
                categories
        );
    }

    private BudgetCategorySummaryResult toCategorySummary(
            BudgetAllocation allocation,
            BudgetCategory category
    ) {
        return new BudgetCategorySummaryResult(
                category.getId(),
                category.getName(),
                allocation.getTotalAmount(),
                allocation.getUsedAmount(),
                allocation.getRemainingAmount(),
                calculateUsageRate(
                        allocation.getUsedAmount(),
                        allocation.getTotalAmount()
                )
        );
    }

    private BigDecimal calculateUsageRate(
            BigDecimal usedAmount,
            BigDecimal totalAmount
    ) {
        if (usedAmount == null
                || totalAmount == null
                || totalAmount.compareTo(
                BigDecimal.ZERO
        ) == 0) {
            return BigDecimal.ZERO;
        }

        return usedAmount
                .multiply(
                        ONE_HUNDRED
                )
                .divide(
                        totalAmount,
                        2,
                        RoundingMode.HALF_UP
                );
    }
}