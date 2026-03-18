package com.example.dtx.controller;

import com.example.dtx.entity.Order;
import com.example.dtx.service.LocalMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 本地消息表模式控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/local-message")
@RequiredArgsConstructor
public class LocalMessageController {

    private final LocalMessageService messageService;

    /**
     * 创建订单（本地消息表模式）
     */
    @PostMapping("/order")
    public ResponseEntity<?> createOrder(
            @RequestParam Long userId,
            @RequestParam Long productId,
            @RequestParam Integer quantity,
            @RequestParam BigDecimal amount) {
        
        try {
            Order order = messageService.createOrder(userId, productId, quantity, amount);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "订单创建成功，消息已记录");
            result.put("data", order);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "订单创建失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取本地消息表说明
     */
    @GetMapping("/info")
    public ResponseEntity<?> getInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "本地消息表模式");
        info.put("description", "将分布式事务转换为本地事务 + 消息投递");
        
        Map<String, String> flow = new HashMap<>();
        flow.put("1", "业务操作和消息记录在同一个本地事务中");
        flow.put("2", "定时任务扫描待发送消息");
        flow.put("3", "发送消息到MQ");
        flow.put("4", "消费方处理业务");
        flow.put("5", "消费方确认消费成功");
        info.put("flow", flow);
        
        info.put("advantages", new String[]{"实现简单", "无外部依赖", "最终一致性保证", "消息可靠投递"});
        info.put("disadvantages", new String[]{"需要维护消息表", "定时任务轮询有延迟", "消费方需要幂等处理"});
        info.put("applicableScenarios", new String[]{"异步场景", "对实时性要求不高", "不想引入MQ的场景"});
        
        return ResponseEntity.ok(info);
    }
}
