package com.robin.core.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface WebControllerConfig {
    String mainPath() default "";
    String insertPath() default "/create";
    String updatePath() default "/update";
    String deleteLogicPath() default "/deleteLogic";
    String viewPath() default "/view/{id}";
    String jdbcDaoName() default "jdbcDao";
    String transactionManagerName() default "transactionManager";
    String serviceName();
}
