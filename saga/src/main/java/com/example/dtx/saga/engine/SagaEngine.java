package com.example.dtx.saga.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Saga执行引擎
 * 
 * Saga模式说明：
 * 
 * 编排式Saga（Choreography Saga）：
 * - 每个服务完成本地事务后，发送事件通知下一个服务
 * - 无中心协调器，服务间通过消息队列通信
 * 
 * 协调式Saga（Orchestration Saga）：
 * - 由Saga协调器统一调度各个服务的执行
 * - 协调器负责调用服务和处理补偿
 * 
 * 本实现采用协调式Saga
 * 
 * 优点：
 * 1. 长事务支持，无全局锁
 * 2. 异步执行，性能较好
 * 3. 适合微服务架构
 * 
 * 缺点：
 * 1. 最终一致性，非强一致
 * 2. 补偿逻辑复杂
 * 3. 隔离性问题（脏读）
 * 
 * 适用场景：
 * 1. 业务流程长、步骤多的场景
 * 2. 需要异步处理的场景
 * 3. 对实时一致性要求不高的场景
 */
@Slf4j
@Component
public class SagaEngine {

    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    /**
     * 执行Saga事务
     * 
     * @param steps Saga步骤列表
     * @param context 上下文
     * @param <T> 上下文类型
     * @return 执行结果
     */
    public <T> SagaResult<T> execute(List<SagaStep<T>> steps, T context) {
        // 按顺序排序
        List<SagaStep<T>> sortedSteps = new ArrayList<>(steps);
        sortedSteps.sort((a, b) -> Integer.compare(a.getOrder(), b.getOrder()));
        
        List<String> executedSteps = new ArrayList<>();
        
        log.info("=== Saga事务开始，共{}个步骤 ===", sortedSteps.size());
        
        for (SagaStep<T> step : sortedSteps) {
            log.info("执行步骤: {}", step.getName());
            
            try {
                boolean success = step.getAction().apply(context);
                
                if (success) {
                    executedSteps.add(step.getName());
                    log.info("步骤执行成功: {}", step.getName());
                } else {
                    log.error("步骤执行失败: {}", step.getName());
                    // 执行补偿
                    compensate(executedSteps, sortedSteps, context);
                    return SagaResult.failed(context, step.getName() + "执行失败");
                }
            } catch (Exception e) {
                log.error("步骤执行异常: {}", step.getName(), e);
                // 执行补偿
                compensate(executedSteps, sortedSteps, context);
                return SagaResult.failed(context, e.getMessage());
            }
        }
        
        log.info("=== Saga事务成功完成 ===");
        return SagaResult.success(context);
    }

    /**
     * 异步执行Saga事务
     */
    public <T> CompletableFuture<SagaResult<T>> executeAsync(List<SagaStep<T>> steps, T context) {
        return CompletableFuture.supplyAsync(() -> execute(steps, context), executor);
    }

    /**
     * 执行补偿操作
     * 
     * @param executedSteps 已执行的步骤
     * @param allSteps 所有步骤
     * @param context 上下文
     * @param <T> 上下文类型
     */
    private <T> void compensate(List<String> executedSteps, List<SagaStep<T>> allSteps, T context) {
        log.info("=== 开始执行补偿操作 ===");
        
        // 反向执行补偿
        List<SagaStep<T>> stepsToCompensate = new ArrayList<>();
        for (String stepName : executedSteps) {
            allSteps.stream()
                    .filter(s -> s.getName().equals(stepName))
                    .findFirst()
                    .ifPresent(stepsToCompensate::add);
        }
        Collections.reverse(stepsToCompensate);
        
        for (SagaStep<T> step : stepsToCompensate) {
            try {
                log.info("执行补偿: {}", step.getName());
                if (step.getCompensation() != null) {
                    step.getCompensation().apply(context);
                }
            } catch (Exception e) {
                log.error("补偿执行失败: {}", step.getName(), e);
                // 补偿失败需要记录，后续人工处理或定时重试
            }
        }
        
        log.info("=== 补偿操作执行完成 ===");
    }

    /**
     * Saga执行结果
     */
    public static class SagaResult<T> {
        private final boolean success;
        private final T context;
        private final String message;

        private SagaResult(boolean success, T context, String message) {
            this.success = success;
            this.context = context;
            this.message = message;
        }

        public static <T> SagaResult<T> success(T context) {
            return new SagaResult<>(true, context, "Success");
        }

        public static <T> SagaResult<T> failed(T context, String message) {
            return new SagaResult<>(false, context, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public T getContext() {
            return context;
        }

        public String getMessage() {
            return message;
        }
    }
}
