package com.ohgiraffer.global.aop.file;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * MultipartFile 파라미터에 붙여서 업로드 허용 최대 크기를 지정한다.
 *
 * <p>MaxFileSizeAspect가 컨트롤러 메서드 진입 전에 이 크기를 검사하므로,
 * 메서드 본문에서 file.getBytes()를 호출하기 전에 이미 걸러진다.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface MaxFileSize {

    long value();
}