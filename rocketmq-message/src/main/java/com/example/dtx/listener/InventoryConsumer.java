package com.example.dtx.listener;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 库存服务消费者
 * 
 * 消费订单消息，扣减库存
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "order-topic",
        consumerGroup = "inventory-consumer-group"
)
public class InventoryConsumer implements RocketMQListener<String> {

    @Override
    public void onMessage(String message) {
        log.info("=== 库存服务收到消息 ===");
        log.info("消息内容: {}", message);
        
        try {
            JSONObject json = JSON.parseObject(message);
            String orderNo = json.getString("orderNo");
            Long productId = json.getLong("productId");
            Integer quantity = json.getInteger("quantity");
            BigDecimal amount = json.getBigDecimal("amount");
            
            // 幂等性检查
            // 检查是否已处理过该订单
            
            // 执行业务：扣减库存
            log.info("扣减库存: productId={}, quantity={}", productId, quantity);
            
            // 模拟库存扣减
            log.info("库存扣减成功");
            
        } catch (Exception e) {
            log.error("消息处理失败", e);
            throw e; // 抛出异常触发重试
        }
    }
}
