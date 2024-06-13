package com.robin.core.web.spring.anotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RepeatSubmitCheck {
    boolean banIp() default false;
    long allowInterval() default 5000L;
    boolean checkParam() default true;
    int allowAccessNumbers() default 0;
}
