package com.ohgiraffer.consultation.application.service;

import com.ohgiraffer.ai.infrastructure.gemini.GeminiClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConsultationGeminiCaller {

    private final GeminiClient geminiClient;

    @CircuitBreaker(name = "geminiApiConsultation", fallbackMethod = "fallbackOnCallFailure")
    public String call(String prompt) {
        return geminiClient.generateText(prompt);
    }

    private String fallbackOnCallFailure(String prompt, Throwable t) {
        if (t instanceof RuntimeException re) {
            throw re;
        }
        throw new IllegalStateException(t);
    }
}