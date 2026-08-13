package com.ohgiraffer.survey.infrastructure.gemini;

import java.util.List;

public record GeminiSurveySummaryResponse(
        String overview,
        List<String> keyInsights,
        List<String> strengths,
        List<String> improvements,
        List<String> recommendations,
        String qualitativeSummary
) {
}