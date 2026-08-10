package com.ohgiraffer.global.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/* comment.
 *  Redisson 클라이언트 등록
 *  - 분산락(@DistributedLock) 전용, 기존 RedisTemplate과는 별도 커넥션
 *  - spring.data.redis 프로퍼티 재사용 (호스트/포트 이원화 방지)
 */

@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private String port;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + host + ":" + port);
        return Redisson.create(config);
    }

}
