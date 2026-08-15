package com.ohgiraffer.global.aop;

import com.ohgiraffer.auditlog.application.command.RecordAuditLogCommand;
import com.ohgiraffer.auditlog.application.usecase.RecordAuditLogUsecase;
import com.ohgiraffer.global.annotation.Audited;
import com.ohgiraffer.security.user.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@Order(Ordered.LOWEST_PRECEDENCE)
public class AuditLogAspect {

    private final RecordAuditLogUsecase recordAuditLogUsecase;

    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(audited)")
    public Object around(ProceedingJoinPoint joinPoint, Audited audited) throws Throwable {
        String beforeValue = evaluate(audited.beforeValue(), joinPoint, null);

        Object result = joinPoint.proceed();

        String targetId = evaluate(audited.targetId(), joinPoint, result);
        String afterValue = evaluate(audited.afterValue(), joinPoint, result);

        recordAuditLogUsecase.record(new RecordAuditLogCommand(
                audited.domain(), audited.eventType(), resolveActorId(),
                targetId, beforeValue, afterValue
        ));

        return result;
    }

    private String evaluate(String expression, ProceedingJoinPoint joinPoint, Object result) {
        if (expression == null || expression.isBlank()) {
            return null;
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        EvaluationContext context = new MethodBasedEvaluationContext(
                joinPoint.getTarget(), method, joinPoint.getArgs(), discoverer
        );
        context.setVariable("result", result);

        Expression exp = parser.parseExpression(expression);
        Object value = exp.getValue(context);

        return value == null ? null : value.toString();
    }

    private Long resolveActorId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserPrincipal principal)) {
            return null;
        }
        return principal.getId();
    }
}