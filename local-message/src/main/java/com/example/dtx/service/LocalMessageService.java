package com.example.dtx.service;

import com.alibaba.fastjson2.JSON;
import com.example.dtx.entity.LocalMessage;
import com.example.dtx.entity.Order;
import com.example.dtx.mapper.LocalMessageMapper;
import com.example.dtx.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 本地消息表服务
 * 
 * 本地消息表模式说明：
 * 
 * 核心思想：
 * 将分布式事务转换为本地事务 + 消息投递
 * 
 * 执行流程：
 * 1. 业务操作和消息记录在同一个本地事务中完成
 * 2. 定时任务扫描待发送消息
 * 3. 发送消息到MQ
 * 4. 消费方处理业务
 * 5. 消费方确认消费成功
 * 
 * 优点：
 * 1. 实现简单，无外部依赖
 * 2. 最终一致性保证
 * 3. 消息可靠投递
 * 
 * 缺点：
 * 1. 需要维护消息表
 * 2. 定时任务轮询有延迟
 * 3. 消费方需要幂等处理
 * 
 * 适用场景：
 * 1. 异步场景
 * 2. 对实时性要求不高的场景
 * 3. 不想引入MQ的场景
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocalMessageService {

    private final OrderMapper orderMapper;
    private final LocalMessageMapper messageMapper;

    /**
     * 创建订单（本地消息表模式）
     * 
     * 业务操作和消息记录在同一个事务中
     */
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(Long userId, Long productId, Integer quantity, BigDecimal amount) {
        log.info("=== 本地消息表模式 - 创建订单 ===");
        
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
        
        // 2. 记录消息（同一事务）
        LocalMessage message = new LocalMessage();
        message.setMessageId(UUID.randomUUID().toString().replace("-", ""));
        message.setBusinessType("ORDER_CREATED");
        message.setBusinessId(orderNo);
        
        Map<String, Object> content = new HashMap<>();
        content.put("orderNo", orderNo);
        content.put("userId", userId);
        content.put("productId", productId);
        content.put("quantity", quantity);
        content.put("amount", amount);
        message.setContent(JSON.toJSONString(content));
        
        message.setDestination("inventory.deduct");
        message.setStatus(LocalMessage.Status.PENDING.getCode());
        message.setRetryCount(0);
        message.setMaxRetryCount(5);
        message.setCreateTime(LocalDateTime.now());
        message.setUpdateTime(LocalDateTime.now());
        
        messageMapper.insert(message);
        log.info("消息记录成功: messageId={}", message.getMessageId());
        
        log.info("=== 本地事务提交（订单+消息） ===");
        return order;
    }

    /**
     * 查询待发送消息
     */
    public List<LocalMessage> getPendingMessages() {
        return messageMapper.selectPendingMessages();
    }

    /**
     * 发送消息
     */
    public boolean sendMessage(LocalMessage message) {
        log.info("发送消息: messageId={}", message.getMessageId());
        
        try {
            // 更新状态为发送中
            message.setStatus(LocalMessage.Status.SENDING.getCode());
            message.setUpdateTime(LocalDateTime.now());
            messageMapper.updateById(message);
            
            // 模拟发送消息到MQ
            // 实际项目中使用RocketMQ/Kafka等
            log.info("消息发送到MQ: destination={}", message.getDestination());
            
            // 更新状态为已发送
            message.setStatus(LocalMessage.Status.SENT.getCode());
            message.setSendTime(LocalDateTime.now());
            messageMapper.updateById(message);
            
            return true;
        } catch (Exception e) {
            log.error("消息发送失败: messageId={}", message.getMessageId(), e);
            
            // 更新失败状态
            message.setStatus(LocalMessage.Status.FAILED.getCode());
            message.setErrorMsg(e.getMessage());
            message.setRetryCount(message.getRetryCount() + 1);
            messageMapper.updateById(message);
            
            return false;
        }
    }

    /**
     * 处理消息消费确认
     */
    public void confirmConsumed(String messageId) {
        log.info("消息消费确认: messageId={}", messageId);
        
        LocalMessage message = messageMapper.selectByMessageId(messageId);
        if (message != null) {
            message.setStatus(LocalMessage.Status.CONSUMED.getCode());
            message.setUpdateTime(LocalDateTime.now());
            messageMapper.updateById(message);
        }
    }
}
