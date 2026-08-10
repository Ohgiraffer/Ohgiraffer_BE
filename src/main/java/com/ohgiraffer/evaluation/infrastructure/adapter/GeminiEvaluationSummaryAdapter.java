package com.ohgiraffer.evaluation.infrastructure.adapter;

import com.ohgiraffer.ai.infrastructure.gemini.GeminiClient;
import com.ohgiraffer.evaluation.application.port.EvaluationSummaryPort;
import com.ohgiraffer.evaluation.domain.model.EvaluationChange;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 제미나이로 변경 요약을 만든다.
 *
 * <p>팀 공용 {@link GeminiClient} 를 쓴다. 요약은 자유로운 글이라 구조화 출력이 필요 없어
 * {@code generateText} 하나로 충분하다.
 */
@Component
public class GeminiEvaluationSummaryAdapter implements EvaluationSummaryPort {

    private final GeminiClient geminiClient;

    public GeminiEvaluationSummaryAdapter(GeminiClient geminiClient) {
        this.geminiClient = geminiClient;
    }

    @Override
    public String summarize(List<EvaluationChange> changes) {
        if (changes.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "요약할 변경 내역이 없습니다."
            );
        }

        return geminiClient.generateText(
                EvaluationSummaryPromptBuilder.build(changes)
        );
    }
}
