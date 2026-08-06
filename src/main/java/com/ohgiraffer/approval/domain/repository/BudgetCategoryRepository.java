package com.ohgiraffer.approval.domain.repository;

import com.ohgiraffer.approval.domain.model.budget.BudgetCategory;

import java.util.List;
import java.util.Optional;

public interface BudgetCategoryRepository {

    BudgetCategory save(
            BudgetCategory budgetCategory
    );

    Optional<BudgetCategory> findById(
            Long id
    );

    List<BudgetCategory> findByIdIn(
            List<Long> ids
    );

    Optional<BudgetCategory> findByName(
            String name
    );

    List<BudgetCategory> findAll();
}