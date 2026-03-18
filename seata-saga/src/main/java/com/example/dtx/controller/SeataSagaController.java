package com.example.dtx.controller;

import com.example.dtx.service.SeataSagaOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Seata Saga模式控制器
 */
@RestController
@RequestMapping("/api/seata-saga")
@RequiredArgsConstructor
public class SeataSagaController {

    private final SeataSagaOrderService sagaOrderService;

    @PostMapping("/order")
    public ResponseEntity<?> createOrder(
            @RequestParam Long userId,
            @RequestParam Long productId,
            @RequestParam Integer quantity,
            @RequestParam BigDecimal amount) {
        
        try {
            String result = sagaOrderService.createOrder(userId, productId, quantity, amount);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", result.contains("成功"));
            response.put("message", result);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "订单创建失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/info")
    public ResponseEntity<?> getInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "Seata Saga模式");
        info.put("description", "基于状态机引擎的长事务解决方案");
        info.put("features", new String[]{"基于JSON状态机定义", "可视化编排", "支持异步执行", "自动补偿"});
        
        return ResponseEntity.ok(info);
    }
}
