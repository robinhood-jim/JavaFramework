package com.robin.tracer.plugin.datasource;


import brave.Tracing;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.sql.DataSource;
import java.sql.Connection;

@Aspect
public class DataSourceAspect implements ApplicationContextAware {
    private ApplicationContext applicationContext;
    private Tracing tracing;

    @Around("execution(* org.springframework.boot.jdbc.DataSourceBuilder+.build())")
    public Object getProxyDataSource(ProceedingJoinPoint joinPoint) throws Throwable {
        return new ProxyDataSource((DataSource) joinPoint.proceed(),applicationContext.getEnvironment(),tracing);
    }
    @Around("execution(* org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource+.getConnection()) && target(bean)")
    public Object getRoutingConnection(ProceedingJoinPoint joinPoint) throws Throwable {
        return new ProxyConnection((Connection) joinPoint.proceed(),applicationContext.getEnvironment(),tracing);
    }
    @Around("execution(* org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource+.getConnection(..)) && target(bean)")
    public Object getRoutingConnectionByParam(ProceedingJoinPoint joinPoint) throws Throwable {
        return new ProxyConnection((Connection) joinPoint.proceed(),applicationContext.getEnvironment(),tracing);
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        if(applicationContext!=null){
            tracing=applicationContext.getBean(Tracing.class);
        }
    }
}
