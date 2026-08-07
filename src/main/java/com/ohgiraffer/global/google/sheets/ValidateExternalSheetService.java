package com.ohgiraffer.global.google.sheets;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ValidateExternalSheetService implements ValidateExternalSheetUseCase {

    private final ExternalSheetPort externalSheetPort;

    public ValidateExternalSheetService(
            ExternalSheetPort externalSheetPort
    ) {
        this.externalSheetPort = externalSheetPort;
    }

    @Override
    public ExternalSheetValidationResult validate(
            String spreadsheetUrl
    ) {
        if (spreadsheetUrl == null
                || spreadsheetUrl.isBlank()) {
            throw new BusinessException(
                    ErrorCode.GOOGLE_SHEET_INVALID_URL
            );
        }

        String spreadsheetId =
                externalSheetPort.extractSpreadsheetId(
                        spreadsheetUrl
                );

        String spreadsheetTitle =
                externalSheetPort.getSpreadsheetTitle(
                        spreadsheetId
                );

        List<SheetColumn> sheetColumns =
                externalSheetPort.getSheetColumns(
                        spreadsheetId
                );

        if (sheetColumns.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.GOOGLE_SHEET_API_ERROR,
                    "스프레드시트에서 시트 정보를 찾을 수 없습니다."
            );
        }

        return new ExternalSheetValidationResult(
                spreadsheetId,
                spreadsheetTitle,
                sheetColumns
        );
    }
}