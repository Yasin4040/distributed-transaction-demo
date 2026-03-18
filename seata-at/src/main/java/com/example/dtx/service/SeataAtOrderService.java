package com.example.dtx.service;

import com.example.dtx.entity.Order;
import com.example.dtx.mapper.OrderMapper;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Seata AT模式订单服务
 * 
 * AT模式原理：
 * 1. 一阶段：业务SQL直接执行，Seata解析SQL生成前后镜像，写入undo_log
 * 2. 二阶段-成功：异步删除undo_log
 * 3. 二阶段-失败：根据undo_log回滚数据
 * 
 * 优点：
 * 1. 对业务无侵入
 * 2. 自动完成回滚
 * 3. 性能较好
 * 
 * 缺点：
 * 1. 需要Seata Server
 * 2. 需要undo_log表
 * 3. 不支持复杂SQL
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeataAtOrderService {

    private final OrderMapper orderMapper;

    /**
     * 创建订单（Seata AT模式）
     * 
     * @GlobalTransactional 开启全局事务
     */
    @GlobalTransactional(name = "create-order", rollbackFor = Exception.class)
    public Order createOrder(Long userId, Long productId, Integer quantity, BigDecimal amount) {
        log.info("=== Seata AT模式 - 创建订单 ===");
        log.info("XID: {}", io.seata.core.context.RootContext.getXID());
        
        String orderNo = UUID.randomUUID().toString().replace("-", "");
        
        // 创建订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setProductId(productId);
        order.setQuantity(quantity);
        order.setAmount(amount);
        order.setStatus(0);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        
        orderMapper.insert(order);
        log.info("订单创建成功");
        
        // 模拟调用其他服务（库存、账户）
        // 实际项目中使用Feign调用
        
        return order;
    }
}
