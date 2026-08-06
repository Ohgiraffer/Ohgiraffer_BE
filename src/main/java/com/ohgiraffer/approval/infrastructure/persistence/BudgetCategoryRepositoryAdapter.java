package com.ohgiraffer.approval.infrastructure.persistence;

import com.ohgiraffer.approval.domain.model.budget.BudgetCategory;
import com.ohgiraffer.approval.domain.repository.BudgetCategoryRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class BudgetCategoryRepositoryAdapter
        implements BudgetCategoryRepository {

    private final SpringDataBudgetCategoryRepository repository;

    public BudgetCategoryRepositoryAdapter(
            SpringDataBudgetCategoryRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public BudgetCategory save(
            BudgetCategory budgetCategory
    ) {
        BudgetCategoryJpaEntity entity =
                BudgetCategoryJpaEntity.from(
                        budgetCategory
                );

        BudgetCategoryJpaEntity savedEntity =
                repository.saveAndFlush(
                        entity
                );

        return savedEntity.toDomain();
    }

    @Override
    public Optional<BudgetCategory> findById(
            Long id
    ) {
        return repository
                .findById(
                        id
                )
                .map(
                        BudgetCategoryJpaEntity::toDomain
                );
    }

    @Override
    public Optional<BudgetCategory> findByName(
            String name
    ) {
        return repository
                .findByName(
                        name
                )
                .map(
                        BudgetCategoryJpaEntity::toDomain
                );
    }

    @Override
    public List<BudgetCategory> findAll() {
        return repository
                .findAll()
                .stream()
                .map(
                        BudgetCategoryJpaEntity::toDomain
                )
                .toList();
    }
}