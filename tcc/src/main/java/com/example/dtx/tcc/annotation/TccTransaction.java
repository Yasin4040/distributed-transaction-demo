package com.example.dtx.tcc.annotation;

import java.lang.annotation.*;

/**
 * TCC事务注解
 * 
 * 标记在业务方法上，表示该方法需要TCC事务管理
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TccTransaction {
    
    /**
     * 确认方法名
     */
    String confirmMethod();
    
    /**
     * 取消方法名
     */
    String cancelMethod();
    
    /**
     * 事务超时时间（秒）
     */
    int timeout() default 60;
}
