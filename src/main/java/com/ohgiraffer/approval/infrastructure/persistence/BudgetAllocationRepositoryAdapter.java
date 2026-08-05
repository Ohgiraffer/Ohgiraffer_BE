package com.ohgiraffer.approval.infrastructure.persistence;

import com.ohgiraffer.approval.domain.model.budget.BudgetAllocation;
import com.ohgiraffer.approval.domain.repository.BudgetAllocationRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class BudgetAllocationRepositoryAdapter
        implements BudgetAllocationRepository {

    private final SpringDataBudgetAllocationRepository repository;

    public BudgetAllocationRepositoryAdapter(
            SpringDataBudgetAllocationRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public BudgetAllocation save(
            BudgetAllocation budgetAllocation
    ) {
        BudgetAllocationJpaEntity entity =
                BudgetAllocationJpaEntity.from(
                        budgetAllocation
                );

        BudgetAllocationJpaEntity savedEntity =
                repository.saveAndFlush(
                        entity
                );

        return savedEntity.toDomain();
    }

    @Override
    public Optional<BudgetAllocation> findByBudgetCategoryId(
            Long budgetCategoryId
    ) {
        return repository
                .findByBudgetCategoryId(
                        budgetCategoryId
                )
                .map(
                        BudgetAllocationJpaEntity::toDomain
                );
    }

    @Override
    public List<BudgetAllocation> findAll() {
        return repository
                .findAll()
                .stream()
                .map(
                        BudgetAllocationJpaEntity::toDomain
                )
                .toList();
    }
}