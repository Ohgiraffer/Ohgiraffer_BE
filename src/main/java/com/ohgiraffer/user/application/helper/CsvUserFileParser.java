package com.ohgiraffer.user.application.helper;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class CsvUserFileParser implements UserFileParser {

    @Override
    public boolean supports(String filename) {
        return filename != null && filename.toLowerCase().endsWith(".csv");
    }

    @Override
    public List<List<Object>> parse(MultipartFile file) {
        List<List<Object>> rows = new ArrayList<>();

        try (InputStreamReader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser csvParser = CSVFormat.DEFAULT.builder()
                     .setIgnoreEmptyLines(true)
                     .setTrim(true)
                     .build()
                     .parse(reader)) {

            for (CSVRecord record : csvParser) {
                List<Object> row = new ArrayList<>();
                record.forEach(row::add);
                rows.add(row);
            }
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_PARSE_FAILED);
        }

        return rows;
    }
}