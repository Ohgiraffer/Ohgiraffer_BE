package com.ohgiraffer.notice.presentation.api.response;

import com.ohgiraffer.notice.application.port.AiCredentialPort;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI 문장 개선용 제미나이 접속 정보")
public record AiRewriteKeyResponse(

        @Schema(
                description = """
                        제미나이 API 키. 화면이 구글을 직접 부를 때 x-goog-api-key 헤더에 넣는다.
                        """,
                example = "AIza..."
        )
        String apiKey,

        @Schema(
                description = """
                        호출할 모델 이름. 서버 설정을 따르므로 모델을 바꿔도 화면은 그대로 둔다.
                        """,
                example = "gemini-3.6-flash"
        )
        String model
) {

    public static AiRewriteKeyResponse from(AiCredentialPort.AiCredential credential) {
        return new AiRewriteKeyResponse(credential.apiKey(), credential.model());
    }
}
