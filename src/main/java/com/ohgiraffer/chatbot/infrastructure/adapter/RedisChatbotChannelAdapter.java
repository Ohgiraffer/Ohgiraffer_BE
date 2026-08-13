package com.ohgiraffer.chatbot.infrastructure.adapter;

import com.ohgiraffer.chatbot.application.port.ChatbotChannelPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

/*
 * comment.
 *  ChatbotChannelPort 실구현체
 *  - 키 패턴: ai:chatbot:channel:{userId} (ai:briefing:{userId} 네임스페이스 컨벤션과 동일선상)
 *  - TTL 없음 - 채널은 한 번 생성되면 계속 유지되는 값이라 만료 개념 불필요
 */

@Slf4j
@Component
public class RedisChatbotChannelAdapter implements ChatbotChannelPort {

    // Redis 키 접두어
    private static final String KEY_PREFIX = "ai:chatbot:channel:";
    // 채널 URL은 단순 문자열이라 StringRedisTemplate 사용
    private final StringRedisTemplate redisTemplate;

    public RedisChatbotChannelAdapter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 저장된 채널 URL 조회 - 없으면 empty
    @Override
    public Optional<String> findChannelUrl(Long userId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key(userId)));
    }

    // 채널 URL 저장 - TTL 없이 영구 저장
    @Override
    public void saveChannelUrl(Long userId, String channelUrl) {
        redisTemplate.opsForValue().set(key(userId), channelUrl); // 정방향 저장 - userId로 channelUrl 조회용
        redisTemplate.opsForValue().set(REVERSE_KEY_PREFIX + channelUrl, "1"); // 역방향 인덱스도 같이 저장 - channelUrl로 AI비서 채널 여부 판별용
    }

    @Override
    public boolean isChatbotChannel(String channelUrl) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(REVERSE_KEY_PREFIX + channelUrl)); // 역방향 키 존재 여부로 판별
    }

    // userId 기반 Redis 키 생성
    private String key(Long userId) {
        return KEY_PREFIX + userId;
    }

    // 역방향 조회용 키 접두어 - channelUrl로 "AI비서 채널인지" 바로 확인하기 위함
    private static final String REVERSE_KEY_PREFIX = "ai:chatbot:channel:url:";

}
