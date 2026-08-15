package com.ohgiraffer.global.sse;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

/*
 * comment.
 *  SSE 구독 전용 단기 1회용 티켓 발급/검증
 *  - EventSource는 커스텀 헤더(Authorization)를 지원하지 않아 쿼리 파라미터로 인증해야 하는데,
 *    액세스 토큰을 그대로 노출하는 대신 30초 TTL짜리 1회용 랜덤 티켓만 노출시켜 리스크를 최소화함
 *  - 발급(issue)은 기존 JWT 인증 필터를 정상 통과한 요청에서만 호출됨 (POST /notifications/subscribe/ticket)
 *  - 검증(validateAndConsume)은 조회 즉시 삭제해서 재사용을 막음 (1회용)
 *  - userId를 Long으로 그대로 저장하지 않고 String으로 변환해서 저장함
 *    -> RedisConfig의 PolymorphicTypeValidator가 com.ohgiraffer 패키지 하위 타입만 허용하고 있어서,
 *       JDK 타입인 Long을 그대로 저장하면 직렬화/역직렬화 시 타입 검증에 걸릴 위험이 있음
 */

@Component
@RequiredArgsConstructor
public class SseTicketService {

    // 티켓 하나가 살아있는 시간. 발급 직후 곧바로 EventSource 연결에 쓰인다는 전제로 짧게 잡음
    private static final Duration TICKET_TTL = Duration.ofSeconds(30);
    // Redis 키 접두사 - 다른 용도의 키와 충돌 방지
    private static final String KEY_PREFIX = "sse:ticket:";

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 티켓 발급
     * 랜덤 문자열을 생성해 Redis에 userId(String 변환)를 값으로 저장(TTL 30초)하고 티켓 값을 반환
     */
    public String issue(Long userId) {
        String ticket = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(KEY_PREFIX + ticket, String.valueOf(userId), TICKET_TTL);
        return ticket;
    }

    /**
     * 티켓 검증 + 즉시 소비(삭제)
     * getAndDelete로 조회와 삭제를 원자적으로 처리해서, 동시에 같은 티켓으로 두 번 요청이 들어와도
     * 하나만 성공하도록 보장함 (1회용 원칙 유지)
     *
     * @return 유효한 티켓이면 userId, 없거나 만료됐으면 null
     */
    public Long validateAndConsume(String ticket) {
        if (ticket == null || ticket.isBlank()) {
            return null;
        }

        Object value = redisTemplate.opsForValue().getAndDelete(KEY_PREFIX + ticket);
        if (value == null) {
            return null;
        }

        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
