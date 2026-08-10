package com.ohgiraffer.global.aop.lock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/* comment.
 *  분산락 획득 이후 실제 비즈니스 로직을 트랜잭션으로 감싸기 위한 별도 프록시 빈
 *  - @Transactional은 프록시 기반이라 같은 클래스 내부 호출(self-invocation)로는 적용 안 됨
 *  - 그래서 Aspect에서 이 빈을 거쳐서 joinPoint.proceed()를 호출해야 트랜잭션이 실제로 시작됨
 *  - 목표 순서: 분산락 획득 -> [트랜잭션 시작 -> 로직 실행 -> 커밋] -> 분산락 해제
 */

@Component
public class AopForTransaction {

    // REQUIRES_NEW: 호출부가 이미 트랜잭션 안에 있더라도 독립적인 새 트랜잭션으로 실행
    // -> 락이 걸린 로직의 커밋이 외부 트랜잭션의 커밋 시점에 종속되지 않도록 보장
    // (커밋 전에 락이 풀리는 순서 역전을 원천 차단)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Object proceed(ProceedingJoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }

}
