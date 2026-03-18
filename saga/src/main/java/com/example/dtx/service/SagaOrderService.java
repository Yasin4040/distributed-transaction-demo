package com.example.dtx.service;

import com.alibaba.fastjson2.JSON;
import com.example.dtx.entity.SagaTransaction;
import com.example.dtx.mapper.SagaTransactionMapper;
import com.example.dtx.saga.engine.SagaEngine;
import com.example.dtx.saga.engine.SagaStep;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Saga订单服务
 * 
 * Saga模式实现订单创建流程：
 * 1. 创建订单（订单服务）
 * 2. 扣减库存（库存服务）
 * 3. 扣减余额（账户服务）
 * 
 * 每个步骤都有对应的补偿操作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SagaOrderService {

    private final SagaEngine sagaEngine;
    private final SagaTransactionMapper sagaTransactionMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Saga上下文
     */
    @Data
    public static class SagaContext {
        private String sagaId;
        private String orderNo;
        private Long userId;
        private Long productId;
        private Integer quantity;
        private BigDecimal amount;
        private List<String> executedSteps = new ArrayList<>();
    }

    /**
     * 使用Saga模式创建订单
     */
    public SagaEngine.SagaResult<SagaContext> createOrder(Long userId, Long productId, Integer quantity, BigDecimal amount) {
        String sagaId = UUID.randomUUID().toString().replace("-", "");
        
        // 初始化上下文
        SagaContext context = new SagaContext();
        context.setSagaId(sagaId);
        context.setOrderNo(sagaId);
        context.setUserId(userId);
        context.setProductId(productId);
        context.setQuantity(quantity);
        context.setAmount(amount);
        
        // 记录Saga事务
        recordSagaStart(context);
        
        // 定义Saga步骤
        List<SagaStep<SagaContext>> steps = new ArrayList<>();
        
        // 步骤1：创建订单
        steps.add(new SagaStep<>(
                "CREATE_ORDER",
                this::doCreateOrder,
                this::compensateCreateOrder,
                1
        ));
        
        // 步骤2：扣减库存
        steps.add(new SagaStep<>(
                "DEDUCT_INVENTORY",
                this::doDeductInventory,
                this::compensateDeductInventory,
                2
        ));
        
        // 步骤3：扣减余额
        steps.add(new SagaStep<>(
                "DEDUCT_BALANCE",
                this::doDeductBalance,
                this::compensateDeductBalance,
                3
        ));
        
        // 执行Saga
        SagaEngine.SagaResult<SagaContext> result = sagaEngine.execute(steps, context);
        
        // 记录Saga结果
        recordSagaEnd(context, result);
        
        return result;
    }

    /**
     * 执行创建订单
     */
    private boolean doCreateOrder(SagaContext context) {
        log.info("Saga步骤 - 创建订单: sagaId={}", context.getSagaId());
        
        try {
            // 模拟调用订单服务
            // 实际项目中使用Feign或RestTemplate调用
            log.info("调用订单服务创建订单...");
            
            // 模拟成功
            context.getExecutedSteps().add("CREATE_ORDER");
            return true;
        } catch (Exception e) {
            log.error("创建订单失败", e);
            return false;
        }
    }

    /**
     * 补偿创建订单
     */
    private boolean compensateCreateOrder(SagaContext context) {
        log.info("Saga补偿 - 取消订单: sagaId={}", context.getSagaId());
        
        try {
            // 调用订单服务取消订单
            log.info("调用订单服务取消订单...");
            return true;
        } catch (Exception e) {
            log.error("取消订单失败", e);
            return false;
        }
    }

    /**
     * 执行扣减库存
     */
    private boolean doDeductInventory(SagaContext context) {
        log.info("Saga步骤 - 扣减库存: sagaId={}, productId={}, quantity={}", 
                context.getSagaId(), context.getProductId(), context.getQuantity());
        
        try {
            // 模拟调用库存服务
            log.info("调用库存服务扣减库存...");
            
            context.getExecutedSteps().add("DEDUCT_INVENTORY");
            return true;
        } catch (Exception e) {
            log.error("扣减库存失败", e);
            return false;
        }
    }

    /**
     * 补偿扣减库存
     */
    private boolean compensateDeductInventory(SagaContext context) {
        log.info("Saga补偿 - 恢复库存: sagaId={}", context.getSagaId());
        
        try {
            // 调用库存服务恢复库存
            log.info("调用库存服务恢复库存...");
            return true;
        } catch (Exception e) {
            log.error("恢复库存失败", e);
            return false;
        }
    }

    /**
     * 执行扣减余额
     */
    private boolean doDeductBalance(SagaContext context) {
        log.info("Saga步骤 - 扣减余额: sagaId={}, userId={}, amount={}", 
                context.getSagaId(), context.getUserId(), context.getAmount());
        
        try {
            // 模拟调用账户服务
            log.info("调用账户服务扣减余额...");
            
            context.getExecutedSteps().add("DEDUCT_BALANCE");
            return true;
        } catch (Exception e) {
            log.error("扣减余额失败", e);
            return false;
        }
    }

    /**
     * 补偿扣减余额
     */
    private boolean compensateDeductBalance(SagaContext context) {
        log.info("Saga补偿 - 恢复余额: sagaId={}", context.getSagaId());
        
        try {
            // 调用账户服务恢复余额
            log.info("调用账户服务恢复余额...");
            return true;
        } catch (Exception e) {
            log.error("恢复余额失败", e);
            return false;
        }
    }

    /**
     * 记录Saga开始
     */
    @Transactional
    public void recordSagaStart(SagaContext context) {
        SagaTransaction transaction = new SagaTransaction();
        transaction.setSagaId(context.getSagaId());
        transaction.setBusinessType("ORDER");
        transaction.setBusinessId(context.getOrderNo());
        transaction.setStatus(0); // 进行中
        transaction.setParams(JSON.toJSONString(context));
        transaction.setCreateTime(LocalDateTime.now());
        transaction.setUpdateTime(LocalDateTime.now());
        
        sagaTransactionMapper.insert(transaction);
    }

    /**
     * 记录Saga结束
     */
    @Transactional
    public void recordSagaEnd(SagaContext context, SagaEngine.SagaResult<SagaContext> result) {
        SagaTransaction transaction = sagaTransactionMapper.selectById(context.getSagaId());
        if (transaction != null) {
            transaction.setStatus(result.isSuccess() ? 1 : 2);
            transaction.setExecutedSteps(JSON.toJSONString(context.getExecutedSteps()));
            transaction.setErrorMsg(result.getMessage());
            transaction.setUpdateTime(LocalDateTime.now());
            transaction.setEndTime(LocalDateTime.now());
            sagaTransactionMapper.updateById(transaction);
        }
    }
}
