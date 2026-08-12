package com.ohgiraffer.chatbot.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/*
 * comment.
 *  ChatbotMessageSentEventListener(AFTER_COMMIT)가 HTTP 요청 스레드를 점유한 채
 *  Gemini/Sendbird 왕복(로그 실측 기준 최대 4.5초 이상)을 기다리지 않도록 전용 실행기로 분리함
 *  - 동시 실행 수를 제한해 챗봇 트래픽이 몰려도 스레드 폭주로 이어지지 않게 함
 *  - Gemini/Sendbird 개별 호출의 타임아웃 설정은 각 클라이언트(GeminiClient/RestClient) 쪽 구성이 필요 -
 *    현재 이 설정 파일에서는 다루지 않음(별도 확인 필요)
 */

@Configuration
@EnableAsync
public class ChatbotAsyncConfig {

    @Bean("chatbotTaskExecutor")
    public TaskExecutor chatbotTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("chatbot-async-");
        executor.initialize();
        return executor;
    }

}
