package com.ohgiraffer.chatbot;

/*
 * comment.
 *  봇 재생성 후 끊어진 채널 멤버십을 복구 - 봇을 다시 해당 채널에 초대
 */

import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

public class BotReInviteStandalone {

    public static void main(String[] args) {
        String appId = "C5342264-DEBC-4FD7-9F78-58EC3D857665";
        String apiToken = "a7adeec70780358fe78787bc4e8d674e7fe436b5";
        String channelUrl = "sendbird_group_channel_386400352_a5b34e260184660d98e66b1443492c93498aa2fb"; // 테스트 채널

        RestClient restClient = RestClient.builder()
                .baseUrl("https://api-" + appId + ".sendbird.com/v3")
                .defaultHeader("Api-Token", apiToken)
                .defaultHeader("Content-Type", "application/json; charset=utf8")
                .build();

        Map response = restClient.post()
                .uri("/group_channels/{channel_url}/invite", channelUrl)
                .body(Map.of("user_ids", List.of("campflow-ai-assistant")))
                .retrieve()
                .body(Map.class);

        System.out.println("=== 재초대 결과 ===");
        System.out.println(response);
    }

}
