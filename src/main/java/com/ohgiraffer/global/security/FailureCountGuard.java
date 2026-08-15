package com.ohgiraffer.global.security;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class FailureCountGuard {

    private final RedisTemplate<String, String> redisTemplate;

    public void checkNotLocked(String scope, String identifier) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey(scope, identifier)))) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        }
    }

    public void recordFailure(String scope, String identifier, int maxCount,
                              Duration failureWindow, Duration lockDuration) {
        String failureKey = failureKey(scope, identifier);

        Boolean created = redisTemplate.opsForValue()
                .setIfAbsent(failureKey, "1", failureWindow);

        long count;
        if (Boolean.TRUE.equals(created)) {
            count = 1L;
        } else {
            Long incremented = redisTemplate.opsForValue().increment(failureKey);
            count = incremented != null ? incremented : 0L;
        }

        if (count >= maxCount) {
            redisTemplate.opsForValue().set(lockKey(scope, identifier), "LOCKED", lockDuration);
        }
    }

    public void resetFailure(String scope, String identifier) {
        redisTemplate.delete(failureKey(scope, identifier));
    }

    private String failureKey(String scope, String identifier) {
        return "failure:" + scope + ":" + identifier;
    }

    private String lockKey(String scope, String identifier) {
        return "lock:" + scope + ":" + identifier;
    }
}