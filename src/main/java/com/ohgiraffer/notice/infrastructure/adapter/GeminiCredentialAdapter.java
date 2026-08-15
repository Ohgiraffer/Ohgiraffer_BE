package com.ohgiraffer.notice.infrastructure.adapter;

import com.ohgiraffer.ai.infrastructure.gemini.GeminiProperties;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.notice.application.port.AiCredentialPort;
import org.springframework.stereotype.Component;

/**
 * 팀 공용 제미나이 설정에서 접속 정보를 읽는다.
 *
 * <p>이 값은 다른 AI 기능도 함께 쓰는 키다. 화면으로 내보내는 경로는 여기 하나뿐이므로,
 * 나중에 문제가 생기면 이 어댑터를 쓰는 곳부터 확인하면 된다.
 */
@Component
public class GeminiCredentialAdapter implements AiCredentialPort {

    private final GeminiProperties geminiProperties;

    public GeminiCredentialAdapter(GeminiProperties geminiProperties) {
        this.geminiProperties = geminiProperties;
    }

    @Override
    public AiCredential get() {
        String apiKey = geminiProperties.getApiKey();
        String model = geminiProperties.getModel();

        /*
         * 배포 환경에 키가 안 들어간 적이 있다. 빈 값을 그대로 내보내면 화면은 200 을 받고
         * 구글에서 400 을 받아, 어디가 잘못됐는지 프론트가 알 수 없다.
         */
        if (isBlank(apiKey) || isBlank(model)) {
            throw new BusinessException(ErrorCode.AI_CREDENTIAL_NOT_CONFIGURED);
        }

        return new AiCredential(apiKey, model);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
