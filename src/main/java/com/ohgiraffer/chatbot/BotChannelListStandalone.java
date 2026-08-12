package com.ohgiraffer.chatbot;

import org.springframework.web.client.RestClient;

import java.util.Map;

/*
 * comment.
 *  특정 유저(봇 포함)가 실제로 참가(joined) 상태인 채널 목록 조회
 *  - GET /users/{user_id}/my_group_channels
 */

public class BotChannelListStandalone {

    public static void main(String[] args) {
        String appId = "C5342264-DEBC-4FD7-9F78-58EC3D857665";
        String apiToken = "a7adeec70780358fe78787bc4e8d674e7fe436b5";
        String botUserId = "campflow-ai-assistant";

        RestClient restClient = RestClient.builder()
                .baseUrl("https://api-" + appId + ".sendbird.com/v3")
                .defaultHeader("Api-Token", apiToken)
                .build();

        Map response = restClient.get()
                .uri("/users/{user_id}/my_group_channels", botUserId)
                .retrieve()
                .body(Map.class);

        System.out.println("=== 봇이 참가한 채널 목록 ===");
        System.out.println(response);
    }

}
