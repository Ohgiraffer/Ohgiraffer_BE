package com.ohgiraffer.team.infrastructure.notion;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(NotionProperties.class)
public class NotionConfig {

    @Bean
    public RestClient notionRestClient(
            NotionProperties properties
    ) {
        return RestClient.builder()
                .baseUrl("https://api.notion.com/v1")
                .defaultHeader("Authorization", "Bearer " + properties.apiToken())
                .defaultHeader("Notion-Version", properties.apiVersion())
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}