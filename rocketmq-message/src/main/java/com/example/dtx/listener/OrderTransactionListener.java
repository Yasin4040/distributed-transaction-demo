package com.example.dtx.listener;

import com.alibaba.fastjson2.JSON;
import com.example.dtx.entity.Order;
import com.example.dtx.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.messaging.Message;

/**
 * 订单事务消息监听器
 * 
 * 处理本地事务和回查
 */
@Slf4j
@RocketMQTransactionListener
@RequiredArgsConstructor
public class OrderTransactionListener implements RocketMQLocalTransactionListener {

    private final OrderMapper orderMapper;

    /**
     * 执行本地事务
     */
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        log.info("执行本地事务");
        
        try {
            // 本地事务已在Service中完成（订单创建）
            // 这里只需要确认事务状态
            Order order = (Order) arg;
            
            // 检查订单是否存在
            Order existOrder = orderMapper.selectById(order.getId());
            if (existOrder != null) {
                log.info("本地事务成功，提交事务消息");
                return RocketMQLocalTransactionState.COMMIT;
            } else {
                log.warn("订单不存在，回滚事务消息");
                return RocketMQLocalTransactionState.ROLLBACK;
            }
        } catch (Exception e) {
            log.error("本地事务异常", e);
            return RocketMQLocalTransactionState.UNKNOWN;
        }
    }

    /**
     * 本地事务回查
     * 
     * 当Broker长时间未收到本地事务结果时，会触发回查
     */
    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        log.info("本地事务回查");
        
        try {
            String transactionId = msg.getHeaders().get("TRANSACTION_ID", String.class);
            
            // 查询订单状态
            Order order = orderMapper.selectByOrderNo(transactionId);
            
            if (order != null) {
                log.info("回查结果：订单存在，提交事务消息");
                return RocketMQLocalTransactionState.COMMIT;
            } else {
                log.warn("回查结果：订单不存在，回滚事务消息");
                return RocketMQLocalTransactionState.ROLLBACK;
            }
        } catch (Exception e) {
            log.error("回查异常", e);
            return RocketMQLocalTransactionState.UNKNOWN;
        }
    }
}
