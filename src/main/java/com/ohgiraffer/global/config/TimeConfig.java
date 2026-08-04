package com.ohgiraffer.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration(proxyBeanMethods = false)
public class TimeConfig {

    @Bean
    public Clock applicationClock() {
        return Clock.system(
                ZoneId.of("Asia/Seoul")
        );
    }
}