package com.ohgiraffer.approval.application.service;

import com.ohgiraffer.approval.application.port.BudgetSheetPort;
import com.ohgiraffer.approval.application.query.BudgetSheetColumn;
import com.ohgiraffer.approval.application.usecase.BudgetSheetValidationResult;
import com.ohgiraffer.approval.application.usecase.ValidateBudgetSheetUseCase;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ValidateBudgetSheetService
        implements ValidateBudgetSheetUseCase {

    private final BudgetSheetPort budgetSheetPort;

    public ValidateBudgetSheetService(
            BudgetSheetPort budgetSheetPort
    ) {
        this.budgetSheetPort = budgetSheetPort;
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetSheetValidationResult validate(
            String spreadsheetUrl
    ) {
        if (spreadsheetUrl == null
                || spreadsheetUrl.isBlank()) {
            throw new BusinessException(
                    ErrorCode.GOOGLE_SHEET_INVALID_URL
            );
        }

        String spreadsheetId =
                budgetSheetPort.extractSpreadsheetId(
                        spreadsheetUrl
                );

        String spreadsheetTitle =
                budgetSheetPort.getSpreadsheetTitle(
                        spreadsheetId
                );

        List<BudgetSheetColumn> sheetColumns =
                budgetSheetPort.getSheetColumns(
                        spreadsheetId
                );

        if (sheetColumns.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.GOOGLE_SHEET_API_ERROR,
                    "스프레드시트에서 시트 정보를 찾을 수 없습니다."
            );
        }

        return new BudgetSheetValidationResult(
                spreadsheetId,
                spreadsheetTitle,
                sheetColumns
        );
    }
}