package com.ohgiraffer.aiassistant.application.port;

import com.ohgiraffer.aiassistant.domain.model.BriefingSourceData;

public interface BriefingGenerationPort {

    // 원본 데이터를 프롬프트로 조립해 Gemini 호출, 생성된 브리핑 텍스트 반환
    String generate(BriefingSourceData sourceData);

}
