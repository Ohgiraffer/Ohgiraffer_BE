package com.ohgiraffer.consultation.application.service;

import com.ohgiraffer.ai.infrastructure.gemini.GeminiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class ConsultationAiBriefGenerator {

    private static final int MAX_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 500;

    private final GeminiClient geminiClient;

    public Optional<String> generate(String counselorNote) {
        String prompt = buildPrompt(counselorNote);

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return Optional.of(geminiClient.generateText(prompt));
            } catch (Exception e) {
                log.warn("[상담 AI 요약 실패] {}/{}번째 시도", attempt, MAX_ATTEMPTS, e);
                if (attempt < MAX_ATTEMPTS) {
                    sleep();
                }
            }
        }

        return Optional.empty();
    }

    private void sleep() {
        try {
            Thread.sleep(RETRY_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String buildPrompt(String counselorNote) {
        return """
                아래는 상담 진행자가 상담 후 남긴 메모입니다.
                핵심만 3~4문장으로 요약해줘. 상담에서 다룬 주제와, 후속 조치가 필요하면 그것도 포함해줘.
                미사여구 없이 담백하게 작성해줘.

                메모:
                %s
                """.formatted(counselorNote);
    }
}