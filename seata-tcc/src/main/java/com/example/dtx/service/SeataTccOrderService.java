package com.example.dtx.service;

import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Seata TCC模式订单服务
 * 
 * Seata TCC是对TCC模式的标准化实现
 * 
 * @LocalTCC 标记为TCC服务
 */
@Slf4j
@Service
@LocalTCC
public class SeataTccOrderService {

    /**
     * Try阶段
     * 
     * @TwoPhaseBusinessAction 标记为两阶段业务动作
     * name: 事务名称
     * commitMethod: 提交方法名
     * rollbackMethod: 回滚方法名
     */
    @TwoPhaseBusinessAction(name = "create-order-tcc", 
            commitMethod = "commit", 
            rollbackMethod = "rollback")
    public boolean tryCreateOrder(
            @BusinessActionContextParameter(paramName = "orderNo") String orderNo,
            @BusinessActionContextParameter(paramName = "userId") Long userId,
            @BusinessActionContextParameter(paramName = "productId") Long productId,
            @BusinessActionContextParameter(paramName = "quantity") Integer quantity,
            @BusinessActionContextParameter(paramName = "amount") BigDecimal amount) {
        
        log.info("=== Seata TCC Try阶段 ===");
        log.info("orderNo={}, userId={}, productId={}, quantity={}, amount={}", 
                orderNo, userId, productId, quantity, amount);
        
        // 1. 创建订单（初始化状态）
        // 2. 预留库存
        // 3. 预留余额
        
        return true;
    }

    /**
     * Confirm阶段
     */
    public boolean commit(BusinessActionContext context) {
        log.info("=== Seata TCC Confirm阶段 ===");
        log.info("xid={}", context.getXid());
        
        String orderNo = context.getActionContext("orderNo").toString();
        log.info("确认订单: orderNo={}", orderNo);
        
        // 1. 更新订单状态为已确认
        // 2. 确认扣减库存
        // 3. 确认扣减余额
        
        return true;
    }

    /**
     * Rollback阶段
     */
    public boolean rollback(BusinessActionContext context) {
        log.info("=== Seata TCC Rollback阶段 ===");
        log.info("xid={}", context.getXid());
        
        String orderNo = context.getActionContext("orderNo").toString();
        log.info("取消订单: orderNo={}", orderNo);
        
        // 1. 更新订单状态为已取消
        // 2. 释放库存
        // 3. 释放余额
        
        return true;
    }
}
