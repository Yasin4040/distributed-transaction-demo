package com.example.dtx.service;

import com.alibaba.fastjson2.JSON;
import com.example.dtx.entity.Order;
import com.example.dtx.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * RocketMQ事务消息服务
 * 
 * 事务消息原理：
 * 1. 发送半消息（Half Message）
 * 2. 执行本地事务
 * 3. 根据本地事务结果提交或回滚半消息
 * 4. 消费方消费消息执行业务
 * 
 * 优点：
 * 1. 最终一致性
 * 2. 消息可靠投递
 * 3. 性能较好
 * 
 * 缺点：
 * 1. 需要MQ支持事务消息
 * 2. 实现较复杂
 * 3. 消费方需要幂等
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RocketMQOrderService {

    private final OrderMapper orderMapper;
    private final RocketMQTemplate rocketMQTemplate;

    /**
     * 创建订单（事务消息模式）
     */
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(Long userId, Long productId, Integer quantity, BigDecimal amount) {
        log.info("=== 事务消息模式 - 创建订单 ===");
        
        String orderNo = UUID.randomUUID().toString().replace("-", "");
        
        // 1. 创建订单
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
        log.info("订单创建成功: orderNo={}", orderNo);
        
        // 2. 发送事务消息
        Map<String, Object> message = new HashMap<>();
        message.put("orderNo", orderNo);
        message.put("userId", userId);
        message.put("productId", productId);
        message.put("quantity", quantity);
        message.put("amount", amount);
        
        // 发送事务消息
        org.springframework.messaging.Message<String> msg = MessageBuilder
                .withPayload(JSON.toJSONString(message))
                .setHeader("TRANSACTION_ID", orderNo)
                .build();
        
        // 发送半消息
        org.apache.rocketmq.client.producer.TransactionSendResult sendResult = 
                rocketMQTemplate.sendMessageInTransaction("order-topic", msg, order);
        
        log.info("事务消息发送结果: {}" , sendResult.getLocalTransactionState());
        
        return order;
    }
}
