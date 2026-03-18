package com.example.dtx.tcc.coordinator;

import com.example.dtx.entity.TccTransactionLog;
import com.example.dtx.mapper.TccTransactionLogMapper;
import com.example.dtx.service.TccOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * TCC事务协调器
 * 
 * 负责：
 * 1. 管理TCC事务的生命周期
 * 2. 定时恢复超时事务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TccCoordinator {

    private final TccTransactionLogMapper tccLogMapper;
    private final TccOrderService tccOrderService;

    /**
     * 定时恢复超时事务（每30秒执行一次）
     */
    @Scheduled(fixedRate = 30000)
    public void recoverExpiredTransactions() {
        log.info("=== TCC事务恢复任务开始 ===");
        
        List<TccTransactionLog> expiredLogs = tccLogMapper.selectExpiredTransactions(LocalDateTime.now());
        
        if (expiredLogs.isEmpty()) {
            log.info("没有需要恢复的事务");
            return;
        }
        
        log.info("发现 {} 个需要恢复的事务", expiredLogs.size());
        
        for (TccTransactionLog logEntry : expiredLogs) {
            try {
                recoverTransaction(logEntry);
            } catch (Exception e) {
                log.error("恢复事务失败: xid={}", logEntry.getXid(), e);
            }
        }
        
        log.info("=== TCC事务恢复任务完成 ===");
    }

    private void recoverTransaction(TccTransactionLog logEntry) {
        String xid = logEntry.getXid();
        Integer status = logEntry.getStatus();
        
        log.info("恢复事务: xid={}, status={}", xid, status);
        
        // 解析参数
        com.alibaba.fastjson2.JSONObject params = com.alibaba.fastjson2.JSON.parseObject(logEntry.getParams());
        Long userId = params.getLong("userId");
        Long productId = params.getLong("productId");
        Integer quantity = params.getInteger("quantity");
        BigDecimal amount = params.getBigDecimal("amount");
        
        switch (status) {
            case 1: // TRYING - 需要回滚
                log.info("事务处于TRYING状态，执行Cancel");
                tccOrderService.cancelCreateOrder(xid, userId, productId, quantity, amount);
                break;
            case 2: // CONFIRMING - 重试Confirm
                log.info("事务处于CONFIRMING状态，重试Confirm");
                tccOrderService.confirmCreateOrder(xid, userId, productId, quantity, amount);
                break;
            case 3: // CANCELLING - 重试Cancel
                log.info("事务处于CANCELLING状态，重试Cancel");
                tccOrderService.cancelCreateOrder(xid, userId, productId, quantity, amount);
                break;
            default:
                log.warn("未知的事务状态: {}", status);
        }
    }
}
