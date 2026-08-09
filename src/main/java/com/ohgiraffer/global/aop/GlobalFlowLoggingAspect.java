package com.ohgiraffer.global.aop;

import com.ohgiraffer.global.trace.TraceIdFilter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalFlowLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(GlobalFlowLoggingAspect.class);
    private static final String BASE_PACKAGE = "com.ohgiraffer.";

    @Around("""
    (execution(* com.ohgiraffer..presentation..*(..)) ||
    execution(* com.ohgiraffer..application.usecase..*(..)) ||
    execution(* com.ohgiraffer..application.service..*(..)) ||
    execution(* com.ohgiraffer..adapter..*(..)) ||
    execution(* com.ohgiraffer..infrastructure..*(..)))
    && !within(com.ohgiraffer.global..*)
    && !within(*..*Properties)
    && !within(*..*Config)
    """)
    public Object traceGlobalFlow(ProceedingJoinPoint joinPoint) throws Throwable {
        String traceId = MDC.get(TraceIdFilter.TRACE_ID);
        String context = resolveContext(joinPoint);
        String layer = resolveLayer(joinPoint);
        String method = joinPoint.getSignature().toShortString();
        long startedAt = System.currentTimeMillis();

        log.debug("[campflow-흐름][{}] 진입 | 도메인={} | 계층={} | 메서드={}",
                traceId, context, layer, method);

        try {
            Object result = joinPoint.proceed();
            long elapsedMs = System.currentTimeMillis() - startedAt;
            log.debug("[campflow-흐름][{}] 종료 | 도메인={} | 계층={} | 메서드={} | 소요시간={}ms",
                    traceId, context, layer, method, elapsedMs);
            return result;
        } catch (Throwable throwable) {
            long elapsedMs = System.currentTimeMillis() - startedAt;
            log.warn("[campflow-흐름][{}] 예외 발생 | 도메인={} | 계층={} | 메서드={} | 소요시간={}ms | 예외={}",
                    traceId, context, layer, method, elapsedMs, resolveExceptionMessage(throwable));
            throw throwable;
        }
    }

    @Around("execution(* com.ohgiraffer..application.service..*(..))")
    public Object logServiceIo(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!log.isInfoEnabled() || !ServiceIoLoggingHolder.isEnabled()) {
            return joinPoint.proceed();
        }

        String traceId = MDC.get(TraceIdFilter.TRACE_ID);
        String method = joinPoint.getSignature().toShortString();

        log.info("[campflow-서비스][{}] 입력 | 메서드={} | 인자={}",
                traceId, method, formatArgs(joinPoint.getArgs()));

        Object result = joinPoint.proceed();

        log.info("[campflow-서비스][{}] 출력 | 메서드={} | 반환={}",
                traceId, method, formatResult(result));

        return result;
    }

    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "(없음)";
        }
        return Arrays.toString(args);
    }

    private String formatResult(Object result) {
        if (result == null) {
            return "(반환값 없음)";
        }
        return result.toString();
    }

    private String resolveLayer(ProceedingJoinPoint joinPoint) {
        String typeName = joinPoint.getSignature().getDeclaringTypeName();

        if (typeName.contains(".presentation.")) {
            return "컨트롤러";
        }
        if (typeName.contains(".application.usecase.")) {
            return "유스케이스";
        }
        if (typeName.contains(".application.service.")) {
            return "서비스";
        }
        if (typeName.contains(".adapter.")) {
            return "어댑터";
        }
        if (typeName.contains(".infrastructure.")) {
            return "인프라스트럭처";
        }
        if (typeName.startsWith("org.springframework.data.repository")
                || typeName.startsWith("org.springframework.data.jpa.repository")) {
            return "인프라스트럭처";
        }
        return "분류되지 않은 계층";
    }

    private String resolveContext(ProceedingJoinPoint joinPoint) {
        String typeName = joinPoint.getSignature().getDeclaringTypeName();
        if (!typeName.startsWith(BASE_PACKAGE)) {
            return "외부";
        }
        String tail = typeName.substring(BASE_PACKAGE.length());
        int dot = tail.indexOf('.');
        return dot == -1 ? tail : tail.substring(0, dot);
    }

    private String resolveExceptionMessage(Throwable throwable) {
        String message = throwable.getMessage();
        if (message == null || message.isBlank()) {
            return "예외 메시지가 없습니다.";
        }
        return message;
    }
}