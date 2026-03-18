package com.example.dtx.service;

import java.math.BigDecimal;

/**
 * TCC订单服务接口
 * 
 * TCC模式说明：
 * 
 * Try阶段：
 * - 完成所有业务检查（一致性）
 * - 预留必须业务资源（准隔离性）
 * 
 * Confirm阶段：
 * - 真正执行业务
 * - 不做任何业务检查
 * - 只使用Try阶段预留的资源
 * - Confirm操作满足幂等性
 * 
 * Cancel阶段：
 * - 释放Try阶段预留的资源
 * - Cancel操作满足幂等性
 */
public interface TccOrderService {
    
    /**
     * Try阶段 - 尝试创建订单
     * 
     * 业务逻辑：
     * 1. 创建订单（状态为初始化）
     * 2. 预留库存（可用库存减少，预留库存增加）
     * 3. 预留余额（余额减少，冻结金额增加）
     * 
     * @param xid 事务ID
     * @param userId 用户ID
     * @param productId 商品ID
     * @param quantity 数量
     * @param amount 金额
     * @return 是否成功
     */
    boolean tryCreateOrder(String xid, Long userId, Long productId, Integer quantity, BigDecimal amount);
    
    /**
     * Confirm阶段 - 确认订单
     * 
     * 业务逻辑：
     * 1. 更新订单状态为已确认
     * 2. 确认扣减库存（预留库存减少，总库存减少）
     * 3. 确认扣减余额（冻结金额减少）
     * 
     * @param xid 事务ID
     * @param userId 用户ID
     * @param productId 商品ID
     * @param quantity 数量
     * @param amount 金额
     * @return 是否成功
     */
    boolean confirmCreateOrder(String xid, Long userId, Long productId, Integer quantity, BigDecimal amount);
    
    /**
     * Cancel阶段 - 取消订单
     * 
     * 业务逻辑：
     * 1. 更新订单状态为已取消
     * 2. 释放库存（可用库存增加，预留库存减少）
     * 3. 释放余额（余额增加，冻结金额减少）
     * 
     * @param xid 事务ID
     * @param userId 用户ID
     * @param productId 商品ID
     * @param quantity 数量
     * @param amount 金额
     * @return 是否成功
     */
    boolean cancelCreateOrder(String xid, Long userId, Long productId, Integer quantity, BigDecimal amount);
}
