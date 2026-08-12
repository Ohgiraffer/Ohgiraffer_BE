package com.ohgiraffer.chatbot.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/*
 * comment.
 *  AI비서 봇 계정 설정
 *  - botUserId: registerBot() 최초 1회 실행 시 사용한 값과 동일하게 고정 유지
 */

@Component
@ConfigurationProperties(prefix = "ai.chatbot.bot")
public class ChatbotBotProperties {

    private String userId;
    private String nickname = "AI비서";
    private String callbackUrl;

    public String getUserId() { return userId; }
    public String getNickname() { return nickname; }
    public String getCallbackUrl() { return callbackUrl; }

    public void setUserId(String userId) { this.userId = userId; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public void setCallbackUrl(String callbackUrl) { this.callbackUrl = callbackUrl; }

}
