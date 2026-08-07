package com.ohgiraffer.survey.application.summary;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class SurveySummaryFileNameGenerator {

    private static final String FILE_PREFIX =
            "CampFlow_설문결과_";

    private static final String PDF_EXTENSION =
            ".pdf";

    private static final int MAX_TITLE_LENGTH = 60;

    private static final ZoneId KOREA_ZONE_ID =
            ZoneId.of("Asia/Seoul");

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    public String generate(
            String surveyTitle
    ) {
        String normalizedTitle =
                normalizeTitle(surveyTitle);

        String generatedDate =
                LocalDate.now(KOREA_ZONE_ID)
                        .format(DATE_FORMATTER);

        return FILE_PREFIX
                + normalizedTitle
                + "_"
                + generatedDate
                + PDF_EXTENSION;
    }

    private String normalizeTitle(
            String surveyTitle
    ) {
        if (surveyTitle == null
                || surveyTitle.isBlank()) {
            return "설문";
        }

        String normalized =
                Normalizer.normalize(
                        surveyTitle,
                        Normalizer.Form.NFC
                );

        normalized = normalized
                .replaceAll(
                        "[\\\\/:*?\"<>|]",
                        "_"
                )
                .replaceAll(
                        "\\s+",
                        "_"
                )
                .replaceAll(
                        "_+",
                        "_"
                )
                .replaceAll(
                        "^_+|_+$",
                        ""
                );

        if (normalized.isBlank()) {
            return "설문";
        }

        if (normalized.length()
                > MAX_TITLE_LENGTH) {
            normalized = normalized.substring(
                    0,
                    MAX_TITLE_LENGTH
            );
        }

        return normalized;
    }
}