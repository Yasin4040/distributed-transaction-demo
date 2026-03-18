package com.example.dtx.service;

import com.example.dtx.entity.Order;

import java.math.BigDecimal;

/**
 * 订单服务接口
 */
public interface OrderService {
    
    /**
     * 创建订单（XA两阶段提交）
     * 
     * 流程：
     * 1. 创建订单（订单库）
     * 2. 扣减库存（库存库）
     * 3. 扣减余额（账户库）
     * 
     * 如果任何一步失败，全部回滚
     */
    Order createOrder(Long userId, Long productId, Integer quantity, BigDecimal amount);
    
    /**
     * 模拟失败场景
     */
    Order createOrderWithFailure(Long userId, Long productId, Integer quantity, BigDecimal amount);
}
