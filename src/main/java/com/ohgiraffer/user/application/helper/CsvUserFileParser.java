package com.ohgiraffer.user.application.helper;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class CsvUserFileParser implements UserFileParser {

    private static final String DELIMITER = ",";

    @Override
    public boolean supports(String filename) {
        return filename != null && filename.toLowerCase().endsWith(".csv");
    }

    @Override
    public List<List<Object>> parse(MultipartFile file) {
        List<List<Object>> rows = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                String[] tokens = line.split(DELIMITER, -1);
                List<Object> row = new ArrayList<>();
                for (String token : tokens) {
                    row.add(token.trim());
                }
                rows.add(row);
            }
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_PARSE_FAILED);
        }

        return rows;
    }
}