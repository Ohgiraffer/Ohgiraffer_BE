package com.ohgiraffer.global.aop.file;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Aspect
@Component
public class MaxFileSizeAspect {

    /**
     * @RequestPart 또는 @RequestParam으로 받는 컨트롤러 메서드 전체를 대상으로,
     * 파라미터에 @MaxFileSize가 붙어있으면 본문 실행 전에 크기를 검사한다.
     */
    @Before("execution(* com.ohgiraffer..presentation.api.*Controller.*(..))")
    public void checkFileSize(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            MaxFileSize maxFileSize = findAnnotation(parameters[i]);
            if (maxFileSize == null) {
                continue;
            }

            if (!(args[i] instanceof MultipartFile file)) {
                continue;
            }

            if (file.getSize() > maxFileSize.value()) {
                throw new BusinessException(
                        ErrorCode.INVALID_INPUT_VALUE,
                        "파일 크기가 허용 범위(" + (maxFileSize.value() / 1024 / 1024) + "MB)를 초과했습니다."
                );
            }
        }
    }

    private MaxFileSize findAnnotation(Parameter parameter) {
        for (Annotation annotation : parameter.getAnnotations()) {
            if (annotation instanceof MaxFileSize maxFileSize) {
                return maxFileSize;
            }
        }
        return null;
    }
}