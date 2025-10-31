package com.robin.core.base.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ServerlessFunction {
    String value() default "";
    String initFunc() default "";
    String initParam() default "";
    String method() default "";
}
