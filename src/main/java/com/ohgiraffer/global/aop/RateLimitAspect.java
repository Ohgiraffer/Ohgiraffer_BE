package com.ohgiraffer.global.aop;

import com.ohgiraffer.global.annotation.RateLimited;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import com.ohgiraffer.security.user.CustomUserPrincipal;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RedisTemplate<String, String> redisTemplate;

    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(rateLimited)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimited rateLimited) throws Throwable {
        String identifier = resolveIdentifier(joinPoint, rateLimited);
        String redisKey = "rate_limit:" + rateLimited.key() + ":" + identifier;

        if (!tryConsume(redisKey, rateLimited.limit(), rateLimited.windowSeconds())) {
            throw new BusinessException(ErrorCode.RATE_LIMIT_EXCEEDED);
        }

        return joinPoint.proceed();
    }

    private boolean tryConsume(String redisKey, int limit, int windowSeconds) {
        long now = System.currentTimeMillis();
        long windowStartMillis = now - (windowSeconds * 1000L);

        redisTemplate.opsForZSet().removeRangeByScore(redisKey, 0, windowStartMillis);

        Long count = redisTemplate.opsForZSet().zCard(redisKey);
        if (count != null && count >= limit) {
            return false;
        }

        String member = now + ":" + UUID.randomUUID();
        redisTemplate.opsForZSet().add(redisKey, member, now);
        redisTemplate.expire(redisKey, windowSeconds, TimeUnit.SECONDS);

        return true;
    }

    private String resolveIdentifier(ProceedingJoinPoint joinPoint, RateLimited rateLimited) {
        Long userId = resolveUserId();
        if (userId != null) {
            return "user:" + userId;
        }
        return "ip:" + resolveClientIp(joinPoint);
    }

    private Long resolveUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserPrincipal principal)) {
            return null;
        }
        return principal.getId();
    }

    private String resolveClientIp(ProceedingJoinPoint joinPoint) {
        // 인증 안 된 요청(로그인 등)은 IP 기준으로 제한 — 실제 요청 컨텍스트는
        // HttpServletRequest를 인자로 받거나 RequestContextHolder로 꺼내야 함
        // (컨트롤러 시그니처 확인 후 구체 구현 조정 필요)
        return "unknown";
    }
}