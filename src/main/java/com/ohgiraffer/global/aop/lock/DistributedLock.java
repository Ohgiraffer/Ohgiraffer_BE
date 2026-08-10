package com.ohgiraffer.global.aop.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/* comment.
 *  분산락이 필요한 메서드에 붙이는 애노테이션
 *  - key: SpEL 표현식, 메서드 파라미터 참조 가능 (ex. "'room:' + #roomId")
 *  - waitTime: 락 획득 대기 시간 - 이 시간 안에 못 잡으면 실패 처리
 *  - leaseTime: 락 최대 점유 시간 - 로직 실행 시간보다 넉넉하게 잡을 것
 *    (leaseTime이 실제 처리 시간보다 짧으면 커밋 전에 락이 풀려서 동시성 보장이 깨짐)
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    String key();                                  // SpEL, ex. "'room:' + #roomId"
    long waitTime() default 5L;
    long leaseTime() default 3L;
    TimeUnit timeUnit() default TimeUnit.SECONDS;

}
