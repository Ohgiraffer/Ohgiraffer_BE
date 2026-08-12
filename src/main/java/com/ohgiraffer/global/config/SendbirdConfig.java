package com.ohgiraffer.global.config;

import com.ohgiraffer.chat.infrastructure.sendbird.SendbirdProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/*
 * comment.
 *  Sendbird Platform API 호출용 RestClient Bean 등록
 *  base URL과 인증 헤더(Api-Token)를 미리 세팅해서, Adapter에서는 path만 지정하면 되게 구성
 */

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SendbirdProperties.class)
public class SendbirdConfig {

    private static final Duration CONNECT_TIMEOUT =
            Duration.ofSeconds(3);

    private static final Duration READ_TIMEOUT =
            Duration.ofSeconds(5);

    @Bean
    public RestClient sendbirdRestClient(
            SendbirdProperties properties
    ) {
        String baseUrl =
                "https://api-" + properties.appId() + ".sendbird.com/v3";

        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(
                CONNECT_TIMEOUT
        );
        requestFactory.setReadTimeout(
                READ_TIMEOUT
        );

        return RestClient.builder()
                .baseUrl(
                        baseUrl
                )
                .requestFactory(
                        requestFactory
                )
                .defaultHeader(
                        "Api-Token",
                        properties.apiToken()
                )
                .defaultHeader(
                        "Content-Type",
                        "application/json"
                )
                .build();
    }
}