package com.ohgiraffer.approval.infrastructure.persistence;

import com.ohgiraffer.approval.domain.model.budget.BudgetCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "budget_category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BudgetCategoryJpaEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(name = "budget_category_id")
    private Long id;

    @Column(
            name = "name",
            nullable = false,
            length = 50
    )
    private String name;

    @Column(
            name = "source",
            nullable = false,
            length = 10
    )
    private String source;

    private BudgetCategoryJpaEntity(
            Long id,
            String name,
            String source
    ) {
        this.id = id;
        this.name = name;
        this.source = source;
    }

    public static BudgetCategoryJpaEntity from(
            BudgetCategory budgetCategory
    ) {
        return new BudgetCategoryJpaEntity(
                budgetCategory.getId(),
                budgetCategory.getName(),
                budgetCategory.getSource()
        );
    }

    public BudgetCategory toDomain() {
        return BudgetCategory.restore(
                id,
                name,
                source
        );
    }
}