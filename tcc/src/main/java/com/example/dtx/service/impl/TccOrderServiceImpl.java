package com.example.dtx.service.impl;

import com.alibaba.fastjson2.JSON;
import com.example.dtx.entity.*;
import com.example.dtx.mapper.*;
import com.example.dtx.service.TccOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * TCC订单服务实现类
 * 
 * TCC模式特点：
 * 
 * 优点：
 * 1. 性能较好，无全局锁
 * 2. 并发度高
 * 3. 资源锁定时间短
 * 
 * 缺点：
 * 1. 业务侵入性强
 * 2. 开发成本高
 * 3. confirm/cancel需要保证幂等
 * 4. 空回滚、悬挂问题需要处理
 * 
 * 适用场景：
 * 1. 对性能要求高的场景
 * 2. 并发量大的互联网应用
 * 3. 需要强一致性的金融业务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TccOrderServiceImpl implements TccOrderService {

    private final OrderMapper orderMapper;
    private final InventoryMapper inventoryMapper;
    private final AccountMapper accountMapper;
    private final TccTransactionLogMapper tccLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean tryCreateOrder(String xid, Long userId, Long productId, Integer quantity, BigDecimal amount) {
        log.info("=== TCC Try阶段 - 开始 ===");
        log.info("xid={}, userId={}, productId={}, quantity={}, amount={}", 
                xid, userId, productId, quantity, amount);
        
        // 1. 幂等性检查 - 检查是否已处理过
        TccTransactionLog existLog = tccLogMapper.selectById(xid);
        if (existLog != null) {
            log.info("Try阶段已执行过，直接返回成功");
            return true;
        }
        
        // 2. 记录TCC事务日志
        TccTransactionLog logEntry = new TccTransactionLog();
        logEntry.setXid(xid);
        logEntry.setBusinessType("ORDER");
        logEntry.setBusinessId(xid);
        logEntry.setStatus(1); // TRYING
        
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("productId", productId);
        params.put("quantity", quantity);
        params.put("amount", amount);
        logEntry.setParams(JSON.toJSONString(params));
        logEntry.setRetryCount(0);
        logEntry.setCreateTime(LocalDateTime.now());
        logEntry.setUpdateTime(LocalDateTime.now());
        logEntry.setExpireTime(LocalDateTime.now().plusMinutes(5));
        tccLogMapper.insert(logEntry);
        
        // 3. 创建订单（状态为初始化）
        Order order = new Order();
        order.setOrderNo(xid);
        order.setUserId(userId);
        order.setProductId(productId);
        order.setQuantity(quantity);
        order.setAmount(amount);
        order.setStatus(0); // 初始化状态
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.insert(order);
        log.info("Try阶段-订单创建成功，状态：初始化");
        
        // 4. 预留库存（可用库存减少，预留库存增加）
        int inventoryResult = inventoryMapper.reserveStock(productId, quantity);
        if (inventoryResult == 0) {
            throw new RuntimeException("库存不足");
        }
        log.info("Try阶段-库存预留成功");
        
        // 5. 预留余额（余额减少，冻结金额增加）
        int accountResult = accountMapper.freezeBalance(userId, amount);
        if (accountResult == 0) {
            throw new RuntimeException("余额不足");
        }
        log.info("Try阶段-余额冻结成功");
        
        log.info("=== TCC Try阶段 - 完成 ===");
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmCreateOrder(String xid, Long userId, Long productId, Integer quantity, BigDecimal amount) {
        log.info("=== TCC Confirm阶段 - 开始 ===");
        log.info("xid={}", xid);
        
        // 1. 幂等性检查
        TccTransactionLog logEntry = tccLogMapper.selectById(xid);
        if (logEntry == null) {
            log.warn("Confirm阶段 - 事务日志不存在，可能Try阶段失败");
            return true; // 空回滚
        }
        if (logEntry.getStatus() == 4) { // CONFIRMED
            log.info("Confirm阶段已执行过，直接返回成功");
            return true;
        }
        
        // 2. 更新事务状态为确认中
        tccLogMapper.updateStatus(xid, 2); // CONFIRMING
        
        // 3. 更新订单状态为已确认
        orderMapper.updateStatus(xid, 1);
        log.info("Confirm阶段-订单状态更新为已确认");
        
        // 4. 确认扣减库存（预留库存减少，总库存减少）
        inventoryMapper.confirmDeduct(productId, quantity);
        log.info("Confirm阶段-库存确认扣减成功");
        
        // 5. 确认扣减余额（冻结金额减少）
        accountMapper.confirmDeduct(userId, amount);
        log.info("Confirm阶段-余额确认扣减成功");
        
        // 6. 更新事务状态为已确认
        tccLogMapper.updateStatus(xid, 4); // CONFIRMED
        
        log.info("=== TCC Confirm阶段 - 完成 ===");
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelCreateOrder(String xid, Long userId, Long productId, Integer quantity, BigDecimal amount) {
        log.info("=== TCC Cancel阶段 - 开始 ===");
        log.info("xid={}", xid);
        
        // 1. 幂等性检查
        TccTransactionLog logEntry = tccLogMapper.selectById(xid);
        if (logEntry == null) {
            log.warn("Cancel阶段 - 事务日志不存在，可能Try阶段失败");
            return true; // 空回滚
        }
        if (logEntry.getStatus() == 5) { // CANCELLED
            log.info("Cancel阶段已执行过，直接返回成功");
            return true;
        }
        
        // 2. 更新事务状态为取消中
        tccLogMapper.updateStatus(xid, 3); // CANCELLING
        
        // 3. 更新订单状态为已取消
        orderMapper.updateStatus(xid, 2);
        log.info("Cancel阶段-订单状态更新为已取消");
        
        // 4. 释放库存（可用库存增加，预留库存减少）
        inventoryMapper.releaseStock(productId, quantity);
        log.info("Cancel阶段-库存释放成功");
        
        // 5. 释放余额（余额增加，冻结金额减少）
        accountMapper.unfreezeBalance(userId, amount);
        log.info("Cancel阶段-余额解冻成功");
        
        // 6. 更新事务状态为已取消
        tccLogMapper.updateStatus(xid, 5); // CANCELLED
        
        log.info("=== TCC Cancel阶段 - 完成 ===");
        return true;
    }
}
