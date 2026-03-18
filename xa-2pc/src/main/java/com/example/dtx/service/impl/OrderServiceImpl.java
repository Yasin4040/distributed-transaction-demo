package com.example.dtx.service.impl;

import com.example.dtx.config.DynamicRoutingDataSource;
import com.example.dtx.entity.Account;
import com.example.dtx.entity.Inventory;
import com.example.dtx.entity.Order;
import com.example.dtx.mapper.AccountMapper;
import com.example.dtx.mapper.InventoryMapper;
import com.example.dtx.mapper.OrderMapper;
import com.example.dtx.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 订单服务实现类
 * 
 * XA两阶段提交说明：
 * 
 * 优点：
 * 1. 强一致性，理论成熟
 * 2. 实现简单，对业务侵入小
 * 3. 支持多种数据库
 * 
 * 缺点：
 * 1. 同步阻塞，性能较差
 * 2. 单点故障（协调者）
 * 3. 资源锁定时间长
 * 4. 不适合高并发场景
 * 
 * 适用场景：
 * 1. 对一致性要求极高的场景
 * 2. 并发量不大的内部系统
 * 3. 短事务场景
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final InventoryMapper inventoryMapper;
    private final AccountMapper accountMapper;

    /**
     * 创建订单 - XA两阶段提交
     * 
     * @Transactional 注解配合JTA事务管理器，实现XA分布式事务
     * 底层使用Atomikos作为XA事务协调器
     */
    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "jtaTransactionManager")
    public Order createOrder(Long userId, Long productId, Integer quantity, BigDecimal amount) {
        log.info("=== XA两阶段提交 - 开始创建订单 ===");
        
        // 1. 创建订单
        Order order = new Order();
        order.setOrderNo(UUID.randomUUID().toString().replace("-", ""));
        order.setUserId(userId);
        order.setProductId(productId);
        order.setQuantity(quantity);
        order.setAmount(amount);
        order.setStatus(0); // 待处理
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        
        DynamicRoutingDataSource.setDataSource("order");
        orderMapper.insert(order);
        log.info("阶段1-订单创建成功: orderNo={}", order.getOrderNo());
        
        // 2. 扣减库存
        DynamicRoutingDataSource.setDataSource("inventory");
        int inventoryResult = inventoryMapper.deductStock(productId, quantity);
        if (inventoryResult == 0) {
            throw new RuntimeException("库存不足");
        }
        log.info("阶段1-库存扣减成功: productId={}, quantity={}", productId, quantity);
        
        // 3. 扣减余额
        DynamicRoutingDataSource.setDataSource("account");
        int accountResult = accountMapper.deductBalance(userId, amount);
        if (accountResult == 0) {
            throw new RuntimeException("余额不足");
        }
        log.info("阶段1-余额扣减成功: userId={}, amount={}", userId, amount);
        
        // 4. 更新订单状态为成功
        DynamicRoutingDataSource.setDataSource("order");
        orderMapper.updateStatus(order.getOrderNo(), 1);
        log.info("阶段1-订单状态更新成功");
        
        // XA阶段二：如果所有操作成功，Atomikos会发送Commit指令
        // 如果有异常，会发送Rollback指令
        log.info("=== XA两阶段提交 - 事务准备完成，等待协调器提交 ===");
        
        DynamicRoutingDataSource.clearDataSource();
        return order;
    }

    /**
     * 模拟失败场景 - 用于测试回滚
     */
    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "jtaTransactionManager")
    public Order createOrderWithFailure(Long userId, Long productId, Integer quantity, BigDecimal amount) {
        log.info("=== XA两阶段提交 - 开始创建订单（模拟失败） ===");
        
        // 1. 创建订单
        Order order = new Order();
        order.setOrderNo(UUID.randomUUID().toString().replace("-", ""));
        order.setUserId(userId);
        order.setProductId(productId);
        order.setQuantity(quantity);
        order.setAmount(amount);
        order.setStatus(0);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        
        DynamicRoutingDataSource.setDataSource("order");
        orderMapper.insert(order);
        log.info("阶段1-订单创建成功: orderNo={}", order.getOrderNo());
        
        // 2. 扣减库存
        DynamicRoutingDataSource.setDataSource("inventory");
        int inventoryResult = inventoryMapper.deductStock(productId, quantity);
        if (inventoryResult == 0) {
            throw new RuntimeException("库存不足");
        }
        log.info("阶段1-库存扣减成功: productId={}, quantity={}", productId, quantity);
        
        // 3. 模拟异常，触发回滚
        log.info("模拟业务异常，触发XA回滚...");
        throw new RuntimeException("模拟业务异常，测试XA回滚");
    }
}
