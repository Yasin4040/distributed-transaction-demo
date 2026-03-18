package com.example.dtx.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 最大努力通知服务
 * 
 * 最大努力通知原理：
 * 1. 消息生产者（通知方）本地事务执行成功后发送消息
 * 2. 消息消费者（被通知方）接收消息并处理
 * 3. 生产者通过定时任务不断重试，直到收到消费者的确认
 * 4. 达到最大重试次数后放弃
 * 
 * 适用场景：
 * 1. 对一致性要求不高的场景
 * 2. 通知类业务（支付回调、短信通知等）
 * 3. 可容忍一定延迟的场景
 */
@Slf4j
@Service
public class NotificationService {

    private final AtomicInteger notificationCounter = new AtomicInteger(0);

    /**
     * 发送通知
     */
    public boolean sendNotification(String businessId, String content) {
        log.info("发送通知: businessId={}", businessId);
        
        try {
            // 模拟发送通知
            log.info("通知内容: {}", content);
            
            // 记录通知记录
            // 实际项目中需要持久化到数据库
            
            return true;
        } catch (Exception e) {
            log.error("发送通知失败", e);
            return false;
        }
    }

    /**
     * 定时重试通知（每10秒执行一次）
     */
    @Scheduled(fixedRate = 10000)
    public void retryNotification() {
        log.debug("=== 扫描待重试通知 ===");
        
        // 查询待重试的通知记录
        // 根据重试次数和间隔时间筛选
        
        // 模拟重试
        int count = notificationCounter.incrementAndGet();
        if (count % 10 == 0) {
            log.info("第{}次重试扫描", count);
        }
    }

    /**
     * 接收确认回调
     */
    public void confirmNotification(String businessId) {
        log.info("收到确认: businessId={}", businessId);
        
        // 更新通知记录状态为已确认
        // 停止对该业务的重试
    }
}
