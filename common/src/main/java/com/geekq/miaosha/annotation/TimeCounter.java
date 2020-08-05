package com.geekq.miaosha.annotation;

import java.lang.annotation.*;

/**
 * 计算查询时间
 *
 * @see CounterAspect
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface TimeCounter {

}
