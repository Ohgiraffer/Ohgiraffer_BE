package com.ohgiraffer.chat.infrastructure.sendbird;

import org.springframework.boot.context.properties.ConfigurationProperties;

/*
 * comment.
 *  Sendbird Platform API 연동 설정값
 *  application.yaml의 sendbird.* 하위 값과 바인딩됨
 *  (app-id -> appId, api-token -> apiToken 자동 매핑)
 */

@ConfigurationProperties(prefix = "sendbird")
public record SendbirdProperties(
        String appId,
        String apiToken
) {

    public SendbirdProperties {
        validateRequiredValue(
                appId,
                "Sendbird app ID"
        );
        validateRequiredValue(
                apiToken,
                "Sendbird API token"
        );

        appId =
                appId.trim();

        apiToken =
                apiToken.trim();
    }

    private static void validateRequiredValue(
            String value,
            String name
    ) {
        if (value == null
                || value.isBlank()) {
            throw new IllegalStateException(
                    name + "가 설정되지 않았습니다."
            );
        }
    }
}