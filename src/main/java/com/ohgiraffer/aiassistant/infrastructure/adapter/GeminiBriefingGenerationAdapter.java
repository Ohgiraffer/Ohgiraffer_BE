package com.ohgiraffer.aiassistant.infrastructure.adapter;

import com.ohgiraffer.ai.infrastructure.gemini.GeminiClient;
import com.ohgiraffer.aiassistant.application.port.BriefingGenerationPort;
import com.ohgiraffer.aiassistant.domain.model.BriefingSourceData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/* comment.
 *  BriefingGenerationPort 실구현체
 *  - BriefingPromptBuilder로 프롬프트 문자열 조립 후 기존 GeminiClient로 호출
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiBriefingGenerationAdapter implements BriefingGenerationPort {

    private final GeminiClient geminiClient;                // 기존 Gemini REST 호출 클라이언트 재사용
    private final BriefingPromptBuilder briefingPromptBuilder;

    @Override
    public String generate(BriefingSourceData sourceData) {
        String prompt = briefingPromptBuilder.build(sourceData);
        return geminiClient.generateText(prompt);
    }

}
