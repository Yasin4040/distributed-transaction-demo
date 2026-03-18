package com.example.dtx.service;

import io.seata.saga.engine.StateMachineEngine;
import io.seata.saga.statelang.domain.ExecutionStatus;
import io.seata.saga.statelang.domain.StateMachineInstance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Seata Saga模式订单服务
 * 
 * Seata Saga是基于状态机引擎的长事务解决方案
 * 
 * 特点：
 * 1. 基于JSON状态机定义
 * 2. 可视化编排
 * 3. 支持异步执行
 * 4. 自动补偿
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeataSagaOrderService {

    private final StateMachineEngine stateMachineEngine;

    /**
     * 创建订单（Seata Saga模式）
     */
    public String createOrder(Long userId, Long productId, Integer quantity, BigDecimal amount) {
        log.info("=== Seata Saga模式 - 创建订单 ===");
        
        // 准备参数
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("productId", productId);
        params.put("quantity", quantity);
        params.put("amount", amount);
        
        // 启动状态机
        StateMachineInstance inst = stateMachineEngine.start(
                "CreateOrderSaga",  // 状态机名称
                null,               // 当前登录用户
                params              // 参数
        );
        
        log.info("Saga执行结果: {}", inst.getStatus());
        
        if (ExecutionStatus.SU.equals(inst.getStatus())) {
            return "订单创建成功";
        } else {
            return "订单创建失败: " + inst.getStatus();
        }
    }
}
