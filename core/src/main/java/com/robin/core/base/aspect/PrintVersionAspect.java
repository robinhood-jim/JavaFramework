package com.robin.core.base.aspect;

import com.robin.core.base.annotation.PrintVersion;
import com.robin.core.version.VersionInfo;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;


@Order(1)
@Aspect
@Slf4j
public class PrintVersionAspect {
    //@Around("@annotation(printVersion)")
    public Object printVersionProcess(ProceedingJoinPoint proceedingJoinPoint, PrintVersion printVersion) throws Throwable {
        log.debug(VersionInfo.getInstance().getVersion());
        return proceedingJoinPoint.proceed();
    }
}
