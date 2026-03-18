package com.example.dtx.controller;

import com.example.dtx.entity.Order;
import com.example.dtx.service.RocketMQOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 事务消息控制器
 */
@RestController
@RequestMapping("/api/rocketmq")
@RequiredArgsConstructor
public class RocketMQController {

    private final RocketMQOrderService orderService;

    @PostMapping("/order")
    public ResponseEntity<?> createOrder(
            @RequestParam Long userId,
            @RequestParam Long productId,
            @RequestParam Integer quantity,
            @RequestParam BigDecimal amount) {
        
        try {
            Order order = orderService.createOrder(userId, productId, quantity, amount);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "订单创建成功，事务消息已发送");
            result.put("data", order);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "订单创建失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/info")
    public ResponseEntity<?> getInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "RocketMQ事务消息");
        info.put("description", "基于RocketMQ事务消息实现最终一致性");
        
        Map<String, String> flow = new HashMap<>();
        flow.put("1", "发送半消息（Half Message）");
        flow.put("2", "执行本地事务");
        flow.put("3", "根据本地事务结果提交或回滚半消息");
        flow.put("4", "消费方消费消息执行业务");
        info.put("flow", flow);
        
        info.put("advantages", new String[]{"最终一致性", "消息可靠投递", "性能较好"});
        info.put("disadvantages", new String[]{"需要MQ支持事务消息", "实现较复杂", "消费方需要幂等"});
        
        return ResponseEntity.ok(info);
    }
}
