package com.ohgiraffer.survey.application.summary;

import java.util.List;

public record SurveyAiSummary(
        boolean generated,
        String overview,
        List<String> keyInsights,
        List<String> strengths,
        List<String> improvements,
        List<String> recommendations,
        String qualitativeSummary
) {

    public SurveyAiSummary {
        overview =
                normalizeText(overview);

        keyInsights =
                safeList(keyInsights);

        strengths =
                safeList(strengths);

        improvements =
                safeList(improvements);

        recommendations =
                safeList(recommendations);

        qualitativeSummary =
                normalizeText(qualitativeSummary);
    }

    public static SurveyAiSummary success(
            String overview,
            List<String> keyInsights,
            List<String> strengths,
            List<String> improvements,
            List<String> recommendations,
            String qualitativeSummary
    ) {
        return new SurveyAiSummary(
                true,
                overview,
                keyInsights,
                strengths,
                improvements,
                recommendations,
                qualitativeSummary
        );
    }

    public static SurveyAiSummary unavailable() {
        return new SurveyAiSummary(
                false,
                "",
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                ""
        );
    }

    public boolean hasQualitativeSummary() {
        return qualitativeSummary != null
                && !qualitativeSummary.isBlank();
    }

    private static String normalizeText(
            String value
    ) {
        if (value == null
                || value.isBlank()) {
            return "";
        }

        return value.trim();
    }

    private static List<String> safeList(
            List<String> values
    ) {
        if (values == null) {
            return List.of();
        }

        return values.stream()
                .filter(value ->
                        value != null
                                && !value.isBlank()
                )
                .map(String::trim)
                .toList();
    }
}