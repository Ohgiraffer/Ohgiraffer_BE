package com.ohgiraffer.global.aop.ratelimit;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.security.user.CustomUserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private static final long LOCK_WAIT_SECONDS = 2L;

    private final RedisTemplate<String, String> redisTemplate;
    private final RedissonClient redissonClient;

    @Around("@annotation(rateLimited)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimited rateLimited) throws Throwable {
        String identifier = resolveIdentifier();
        String redisKey = "rate_limit:" + rateLimited.key() + ":" + identifier;

        if (!tryConsume(redisKey, rateLimited.limit(), rateLimited.windowSeconds())) {
            throw new BusinessException(ErrorCode.RATE_LIMIT_EXCEEDED);
        }

        return joinPoint.proceed();
    }

    private boolean tryConsume(String redisKey, int limit, int windowSeconds) {
        RLock lock = redissonClient.getLock("rate_limit_lock:" + redisKey);

        boolean acquired;
        try {
            acquired = lock.tryLock(LOCK_WAIT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.LOCK_WAIT_INTERRUPTED);
        }

        if (!acquired) {
            return false;
        }

        try {
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
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private String resolveIdentifier() {
        Long userId = resolveUserId();
        if (userId != null) {
            return "user:" + userId;
        }
        return "ip:" + resolveClientIp();
    }

    private Long resolveUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserPrincipal principal)) {
            return null;
        }
        return principal.getId();
    }

    private String resolveClientIp() {
        RequestAttributes raw = RequestContextHolder.getRequestAttributes();
        if (!(raw instanceof ServletRequestAttributes attributes)) {
            return "system";
        }

        HttpServletRequest request = attributes.getRequest();

        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}