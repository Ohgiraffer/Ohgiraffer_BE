package com.ohgiraffer.global.metrics;

import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class MetricsAop {

    private static final String METRIC_PREFIX = "campflow";

    private final CampFlowMetrics campFlowMetrics;

    @Around("execution(* com.ohgiraffer..application.service..*(..))")
    public Object measureServiceTiming(ProceedingJoinPoint joinPoint) throws Throwable {
        String metricName = resolveMetricName(joinPoint);
        Timer.Sample sample = campFlowMetrics.startTimer();
        try {
            return joinPoint.proceed();
        } catch (Throwable t) {
            campFlowMetrics.incrementCounter(metricName + ".failed");
            throw t;
        } finally {
            campFlowMetrics.stopTimer(sample, metricName + ".duration");
        }
    }

     private String resolveMetricName(ProceedingJoinPoint joinPoint) {
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String domain = resolveDomain(joinPoint);

         return METRIC_PREFIX + "." + domain + "." + className + "." + methodName;
    }

    private String resolveDomain(ProceedingJoinPoint joinPoint) {
        String typeName = joinPoint.getSignature().getDeclaringTypeName();
        String prefix = "com.ohgiraffer.";
        if (!typeName.startsWith(prefix)) {
            return "unknown";
        }
        String tail = typeName.substring(prefix.length());
        int dot = tail.indexOf('.');
        return dot == -1 ? tail : tail.substring(0, dot);
    }
}