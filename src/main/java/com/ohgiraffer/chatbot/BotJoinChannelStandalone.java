package com.ohgiraffer.chatbot;

import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/*
 * comment.
 *  봇 전용 "채널 참가" API 사용 - 일반 멤버 초대(invite)와 다른 엔드포인트
 *  - POST /bots/{bot_userid}/channels (공식 봇 튜토리얼에서 사용하는 정식 방법)
 */

public class BotJoinChannelStandalone {

    public static void main(String[] args) {
        String appId = "C5342264-DEBC-4FD7-9F78-58EC3D857665";
        String apiToken = "a7adeec70780358fe78787bc4e8d674e7fe436b5";
        String botUserId = "campflow-ai-assistant";
        String channelUrl = "sendbird_group_channel_386400352_615db76c6b9f2bb2d99e8cf3b658787f476a6279";

        RestClient restClient = RestClient.builder()
                .baseUrl("https://api-" + appId + ".sendbird.com/v3")
                .defaultHeader("Api-Token", apiToken)
                .defaultHeader("Content-Type", "application/json; charset=utf8")
                .build();

        Map response = restClient.post()
                .uri("/bots/{bot_userid}/channels", botUserId)
                .body(Map.of("channel_urls", List.of(channelUrl)))
                .retrieve()
                .body(Map.class);

        System.out.println("=== 봇 채널 참가 결과 ===");
        System.out.println(response);
    }

}
