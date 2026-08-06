package com.ohgiraffer.approval.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataBudgetAllocationRepository
        extends JpaRepository<BudgetAllocationJpaEntity, Long> {

    Optional<BudgetAllocationJpaEntity> findByBudgetCategoryId(
            Long budgetCategoryId
    );
}