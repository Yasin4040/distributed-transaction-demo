package com.example.dtx.saga.engine;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.function.Function;

/**
 * Saga步骤定义
 * 
 * @param <T> 上下文类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SagaStep<T> {
    
    /**
     * 步骤名称
     */
    private String name;
    
    /**
     * 执行操作
     */
    private Function<T, Boolean> action;
    
    /**
     * 补偿操作
     */
    private Function<T, Boolean> compensation;
    
    /**
     * 步骤顺序
     */
    private int order;
}
