package com.ohgiraffer.approval.domain.repository;

import com.ohgiraffer.approval.domain.model.budget.BudgetAllocation;

import java.util.List;
import java.util.Optional;

public interface BudgetAllocationRepository {

    BudgetAllocation save(
            BudgetAllocation budgetAllocation
    );

    Optional<BudgetAllocation> findByBudgetCategoryId(
            Long budgetCategoryId
    );

    List<BudgetAllocation> findAll();
}