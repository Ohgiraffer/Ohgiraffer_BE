package com.ohgiraffer.global.aop.lock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/* comment.
 *  분산락 획득 이후 실제 비즈니스 로직을 트랜잭션으로 감싸기 위한 별도 프록시 빈
 *  - @Transactional은 프록시 기반이라 같은 클래스 내부 호출(self-invocation)로는 적용 안 됨
 *  - 그래서 Aspect에서 이 빈을 거쳐서 joinPoint.proceed()를 호출해야 트랜잭션이 실제로 시작됨
 *  - 목표 순서: 분산락 획득 -> [트랜잭션 시작 -> 로직 실행 -> 커밋] -> 분산락 해제
 */

@Component
public class AopForTransaction {

    @Transactional
    public Object proceed(ProceedingJoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }

}
