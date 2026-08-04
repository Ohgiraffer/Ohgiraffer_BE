package com.ohgiraffer.global.config;

import com.ohgiraffer.chat.infrastructure.adapter.SendbirdProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/*
 * comment.
 *  Sendbird Platform API 호출용 RestClient Bean 등록
 *  base URL과 인증 헤더(Api-Token)를 미리 세팅해서, Adapter에서는 path만 지정하면 되게 구성
 */

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SendbirdProperties.class)
public class SendbirdConfig {

    @Bean
    public RestClient sendbirdRestClient(SendbirdProperties properties) {
        String baseUrl = "https://api-" + properties.appId() + ".sendbird.com/v3";

        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Api-Token", properties.apiToken())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

}
