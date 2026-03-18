package com.example.dtx.controller;

import com.example.dtx.entity.Order;
import com.example.dtx.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 订单控制器
 * 
 * XA两阶段提交测试接口
 */
@RestController
@RequestMapping("/api/xa")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 正常下单 - XA两阶段提交成功场景
     */
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
            result.put("message", "订单创建成功（XA两阶段提交）");
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
     * 模拟失败 - XA两阶段提交回滚场景
     */
    @PostMapping("/order/failure")
    public ResponseEntity<?> createOrderWithFailure(
            @RequestParam Long userId,
            @RequestParam Long productId,
            @RequestParam Integer quantity,
            @RequestParam BigDecimal amount) {
        
        try {
            Order order = orderService.createOrderWithFailure(userId, productId, quantity, amount);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", order);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "订单创建失败，已回滚: " + e.getMessage());
            result.put("xaPhase", "第二阶段 - Rollback");
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取XA两阶段提交说明
     */
    @GetMapping("/info")
    public ResponseEntity<?> getXAInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "XA两阶段提交（2PC）");
        info.put("description", "基于XA协议的分布式事务解决方案");
        info.put("phase1", "准备阶段：协调者询问所有参与者是否可以提交");
        info.put("phase2", "提交阶段：所有参与者返回Yes则提交，任一返回No则回滚");
        info.put("advantages", new String[]{"强一致性", "理论成熟", "对业务侵入小"});
        info.put("disadvantages", new String[]{"同步阻塞", "单点故障", "资源锁定时间长", "性能较差"});
        info.put("applicableScenarios", new String[]{"对一致性要求极高的场景", "并发量不大的内部系统", "短事务场景"});
        
        return ResponseEntity.ok(info);
    }
}
