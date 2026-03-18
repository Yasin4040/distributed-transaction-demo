package com.example.dtx.controller;

import com.example.dtx.entity.Order;
import com.example.dtx.service.SeataAtOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Seata AT模式控制器
 */
@RestController
@RequestMapping("/api/seata-at")
@RequiredArgsConstructor
public class SeataAtController {

    private final SeataAtOrderService orderService;

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
            result.put("message", "订单创建成功（Seata AT模式）");
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
        info.put("name", "Seata AT模式");
        info.put("description", "基于SQL解析的自动补偿模式");
        
        Map<String, String> principle = new HashMap<>();
        principle.put("phase1", "业务SQL直接执行，生成前后镜像，写入undo_log");
        principle.put("phase2-success", "异步删除undo_log");
        principle.put("phase2-failure", "根据undo_log回滚数据");
        info.put("principle", principle);
        
        info.put("advantages", new String[]{"对业务无侵入", "自动完成回滚", "性能较好"});
        info.put("disadvantages", new String[]{"需要Seata Server", "需要undo_log表", "不支持复杂SQL"});
        
        return ResponseEntity.ok(info);
    }
}
