package com.ohgiraffer.global.security;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class FailureCountGuard {

    private static final long LOCK_WAIT_SECONDS = 2L;
    private static final long LOCK_LEASE_SECONDS = 3L;

    private final RedisTemplate<String, String> redisTemplate;
    private final RedissonClient redissonClient;

    public void checkNotLocked(String scope, String identifier) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey(scope, identifier)))) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        }
    }

    public void recordFailure(String scope, String identifier, int maxCount,
                              Duration failureWindow, Duration lockDuration) {
        RLock lock = redissonClient.getLock("failure_guard_lock:" + scope + ":" + identifier);

        boolean acquired;
        try {
            acquired = lock.tryLock(LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.LOCK_WAIT_INTERRUPTED);
        }

        if (!acquired) {
            throw new BusinessException(ErrorCode.LOCK_ACQUISITION_FAILED);
        }

        try {
            String failureKey = failureKey(scope, identifier);
            Long count = redisTemplate.opsForValue().increment(failureKey);

            if (count != null && count == 1L) {
                redisTemplate.expire(failureKey, failureWindow);
            }

            if (count != null && count >= maxCount) {
                redisTemplate.opsForValue().set(lockKey(scope, identifier), "LOCKED", lockDuration);
            }
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
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