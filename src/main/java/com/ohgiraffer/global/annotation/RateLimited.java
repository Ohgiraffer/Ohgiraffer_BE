package com.ohgiraffer.global.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {

    String key();

    int limit();

    int windowSeconds();
}