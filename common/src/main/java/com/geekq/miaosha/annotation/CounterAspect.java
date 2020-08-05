package com.geekq.miaosha.annotation;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * 计算消耗的时间
 */

@Slf4j
@Aspect
public class CounterAspect {

    /**
     * 定义切点，定位到@Log注解的地方
     */
    @Pointcut("@annotation(com.geekq.miaosha.annotation.TimeCounter)")
    public void counterPointCut() {

    }

    /**
     * 注解打印日志
     */
    @Around("counterPointCut()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long end = System.currentTimeMillis();
            log.info("## method {} costs {} ##", joinPoint.getSignature().getName(), end - start);
        }
    }
}
