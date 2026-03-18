package com.example.dtx.controller;

import com.example.dtx.service.SeataTccOrderService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Seata TCC模式控制器
 */
@RestController
@RequestMapping("/api/seata-tcc")
@RequiredArgsConstructor
public class SeataTccController {

    private final SeataTccOrderService tccOrderService;

    @PostMapping("/order")
    @GlobalTransactional(name = "create-order-tcc", rollbackFor = Exception.class)
    public ResponseEntity<?> createOrder(
            @RequestParam Long userId,
            @RequestParam Long productId,
            @RequestParam Integer quantity,
            @RequestParam BigDecimal amount) {
        
        try {
            String orderNo = UUID.randomUUID().toString().replace("-", "");
            
            boolean result = tccOrderService.tryCreateOrder(orderNo, userId, productId, quantity, amount);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", result);
            response.put("message", result ? "订单创建成功（Seata TCC）" : "订单创建失败");
            response.put("orderNo", orderNo);
            
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
        info.put("name", "Seata TCC模式");
        info.put("description", "Seata对TCC模式的标准化实现");
        info.put("advantages", new String[]{"标准化TCC", "与Seata生态集成", "支持幂等控制"});
        
        return ResponseEntity.ok(info);
    }
}
