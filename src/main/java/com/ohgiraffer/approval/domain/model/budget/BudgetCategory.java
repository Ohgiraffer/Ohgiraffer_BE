package com.ohgiraffer.approval.domain.model.budget;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BudgetCategory {

    private final Long id;
    private String name;
    private String source;

    public static BudgetCategory createFromSheet(
            String name
    ) {
        BudgetCategory budgetCategory =
                new BudgetCategory(
                        null
                );

        budgetCategory.name = name;
        budgetCategory.source = "SHEET";

        return budgetCategory;
    }

    public static BudgetCategory restore(
            Long id,
            String name,
            String source
    ) {
        BudgetCategory budgetCategory =
                new BudgetCategory(
                        id
                );

        budgetCategory.name = name;
        budgetCategory.source = source;

        return budgetCategory;
    }

    public void updateName(
            String name
    ) {
        this.name = name;
    }
}