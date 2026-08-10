package com.ohgiraffer.global.aop.lock;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/* comment.
 *  @DistributedLock 붙은 메서드를 감싸는 Aspect
 *  - 처리 순서: 락 획득(tryLock) -> AopForTransaction 통해 트랜잭션 실행 -> 커밋 -> 락 해제(finally)
 *  - @Order(1)로 고정한 이유: 트랜잭션 AOP보다 먼저 실행되어야
 *    "분산락 해제가 트랜잭션 커밋보다 먼저 일어나는" 순서 역전을 막을 수 있음
 *    (역전되면 커밋 전 데이터에 다른 스레드가 접근할 수 있어 동시성 보장이 깨짐)
 */

@Slf4j
@Aspect
@Component
@Order(1) // 트랜잭션 AOP(기본 Order.LOWEST~ Integer.MAX_VALUE)보다 먼저 실행되도록 앞순위 고정
@RequiredArgsConstructor
public class DistributedLockAspect {

    private final RedissonClient redissonClient;
    private final AopForTransaction aopForTransaction;

    @Around("@annotation(distributedLock)")
    public Object lock(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String key = (String) CustomSpringELParser.getDynamicValue(
                signature.getParameterNames(),
                joinPoint.getArgs(),
                distributedLock.key()
        );

        RLock rLock = redissonClient.getLock(key);
        boolean acquired = false;

        try {
            acquired = rLock.tryLock(
                    distributedLock.waitTime(),
                    distributedLock.leaseTime(),
                    distributedLock.timeUnit()
            );

            if (!acquired) {
                log.warn("[DistributedLock] 락 획득 실패 | key={}", key);
                throw new BusinessException(ErrorCode.LOCK_ACQUISITION_FAILED);
            }

            return aopForTransaction.proceed(joinPoint);

        } finally {
            if (acquired && rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
        }
    }

}
