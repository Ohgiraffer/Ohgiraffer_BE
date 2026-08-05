package com.ohgiraffer.approval.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataBudgetCategoryRepository
        extends JpaRepository<BudgetCategoryJpaEntity, Long> {

    Optional<BudgetCategoryJpaEntity> findByName(
            String name
    );
}