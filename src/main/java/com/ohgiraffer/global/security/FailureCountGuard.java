package com.ohgiraffer.global.security;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FailureCountGuard {

    // 로그인 실패 횟수를 세다가 너무 많이 틀리면 계정을 잠근다
    // 1.실패 카운터 +1
    // 2. 이번이 첫 실패면 카운터에 유효기간을 걸어둠 (10분 지나면 자동 리셋)
    // 3. 실패 횟수가 5번을 넘으면 15분간 로그인 차단
    private static final String RECORD_FAILURE_SCRIPT =
            "local count = redis.call('INCR', KEYS[1]) " +
                    "if count == 1 then redis.call('EXPIRE', KEYS[1], ARGV[1]) end " +
                    "if count >= tonumber(ARGV[2]) then redis.call('SET', KEYS[2], 'LOCKED', 'EX', ARGV[3]) end " +
                    "return count";

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisScript<Long> recordFailureScript =
            new DefaultRedisScript<>(RECORD_FAILURE_SCRIPT, Long.class);

    public void checkNotLocked(String scope, String identifier) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey(scope, identifier)))) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        }
    }

    public void recordFailure(String scope, String identifier, int maxCount,
                              Duration failureWindow, Duration lockDuration) {
        redisTemplate.execute(
                recordFailureScript,
                List.of(failureKey(scope, identifier), lockKey(scope, identifier)),
                String.valueOf(failureWindow.getSeconds()),
                String.valueOf(maxCount),
                String.valueOf(lockDuration.getSeconds())
        );
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