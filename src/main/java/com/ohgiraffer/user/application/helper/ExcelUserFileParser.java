package com.ohgiraffer.user.application.helper;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class ExcelUserFileParser implements UserFileParser {

    @Override
    public boolean supports(String filename) {
        if (filename == null) {
            return false;
        }
        String lower = filename.toLowerCase();
        return lower.endsWith(".xlsx") || lower.endsWith(".xls");
    }

    @Override
    public List<List<Object>> parse(MultipartFile file) {
        List<List<Object>> rows = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                int lastCellNum = Math.max(row.getLastCellNum(), 0);
                List<Object> rowData = new ArrayList<>();
                boolean isEmptyRow = true;

                for (int j = 0; j < lastCellNum; j++) {
                    Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    String value = formatter.formatCellValue(cell).trim();
                    if (!value.isBlank()) {
                        isEmptyRow = false;
                    }
                    rowData.add(value);
                }

                if (!isEmptyRow) {
                    rows.add(rowData);
                }
            }
        } catch (EncryptedDocumentException e) {
            throw new BusinessException(ErrorCode.FILE_PARSE_FAILED, "암호가 설정된 엑셀 파일은 업로드할 수 없습니다.");
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_PARSE_FAILED);
        }

        return rows;
    }
}