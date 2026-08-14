package com.ohgiraffer.global.auditlog;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {

    String domain();

    String eventType();

    String targetId() default "";

    String beforeValue() default "";

    String afterValue() default "";
}