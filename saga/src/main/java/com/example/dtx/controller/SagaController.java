package com.example.dtx.controller;

import com.example.dtx.saga.engine.SagaEngine;
import com.example.dtx.service.SagaOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Saga模式控制器
 * 
 * Saga分布式事务测试接口
 */
@Slf4j
@RestController
@RequestMapping("/api/saga")
@RequiredArgsConstructor
public class SagaController {

    private final SagaOrderService sagaOrderService;

    /**
     * Saga模式创建订单
     */
    @PostMapping("/order")
    public ResponseEntity<?> createOrder(
            @RequestParam Long userId,
            @RequestParam Long productId,
            @RequestParam Integer quantity,
            @RequestParam BigDecimal amount) {
        
        try {
            SagaEngine.SagaResult<SagaOrderService.SagaContext> result = 
                    sagaOrderService.createOrder(userId, productId, quantity, amount);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            response.put("sagaId", result.getContext().getSagaId());
            response.put("executedSteps", result.getContext().getExecutedSteps());
            
            if (result.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Saga执行异常: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取Saga说明
     */
    @GetMapping("/info")
    public ResponseEntity<?> getSagaInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "Saga模式");
        info.put("description", "长事务解决方案，通过补偿机制保证最终一致性");
        
        Map<String, String> types = new HashMap<>();
        types.put("choreography", "编排式Saga：服务间通过事件通知");
        types.put("orchestration", "协调式Saga：由协调器统一调度");
        info.put("types", types);
        
        info.put("advantages", new String[]{"长事务支持", "无全局锁", "异步执行", "适合微服务"});
        info.put("disadvantages", new String[]{"最终一致性", "补偿逻辑复杂", "隔离性问题（脏读）"});
        info.put("applicableScenarios", new String[]{"业务流程长的场景", "需要异步处理", "对实时一致性要求不高"});
        
        return ResponseEntity.ok(info);
    }
}
