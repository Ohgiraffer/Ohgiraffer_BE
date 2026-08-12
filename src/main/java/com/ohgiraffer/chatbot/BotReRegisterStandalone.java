package com.ohgiraffer.chatbot;

import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

/*
 * comment.
 *  기존 봇 삭제 후, channel_invitation_preference를 명시적으로 0으로 지정해서 재생성
 *  - Sendbird 커뮤니티 사례(channel_invitation_preference 미지정 시 1로 초기화되는 버그) 대응
 *  - bot_type도 marketer로 명시 (CUSTOMIZED_BOT 방지)
 */

public class BotReRegisterStandalone {

    public static void main(String[] args) {
        String appId = "C5342264-DEBC-4FD7-9F78-58EC3D857665";
        String apiToken = "a7adeec70780358fe78787bc4e8d674e7fe436b5";
        String botUserId = "campflow-ai-assistant";

        RestClient restClient = RestClient.builder()
                .baseUrl("https://api-" + appId + ".sendbird.com/v3")
                .defaultHeader("Api-Token", apiToken)
                .defaultHeader("Content-Type", "application/json; charset=utf8")
                .build();

        // 기존 봇 삭제
        try {
            restClient.delete().uri("/bots/{bot_userid}", botUserId)
                    .retrieve().toBodilessEntity();
            System.out.println("기존 봇 삭제 완료");
        } catch (Exception e) {
            System.out.println("삭제 실패 또는 이미 없음: " + e.getMessage());
        }

        // channel_invitation_preference 명시해서 재생성
        Map<String, Object> body = new HashMap<>();
        body.put("bot_type", "marketer");
        body.put("bot_userid", botUserId);
        body.put("bot_nickname", "AI비서");
        body.put("bot_callback_url", "https://duration-energy-shone.ngrok-free.dev/webhooks/sendbird/bot");
        body.put("is_privacy_mode", true);
        body.put("enable_mark_as_read", true);
        body.put("channel_invitation_preference", 0);   // 명시적으로 0 지정 - 이번 수정 핵심

        Map response = restClient.post().uri("/bots").body(body).retrieve().body(Map.class);
        System.out.println("=== 재생성 완료 ===");
        System.out.println(response);
    }

}