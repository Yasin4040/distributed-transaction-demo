package com.example.dtx.controller;

import com.example.dtx.service.TccOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * TCC模式控制器
 * 
 * TCC（Try-Confirm-Cancel）分布式事务测试接口
 */
@Slf4j
@RestController
@RequestMapping("/api/tcc")
@RequiredArgsConstructor
public class TccController {

    private final TccOrderService tccOrderService;

    /**
     * TCC完整流程测试 - 成功场景
     */
    @PostMapping("/order")
    public ResponseEntity<?> createOrder(
            @RequestParam Long userId,
            @RequestParam Long productId,
            @RequestParam Integer quantity,
            @RequestParam BigDecimal amount) {
        
        String xid = UUID.randomUUID().toString().replace("-", "");
        log.info("=== TCC事务开始，xid={} ===", xid);
        
        try {
            // 1. Try阶段
            boolean tryResult = tccOrderService.tryCreateOrder(xid, userId, productId, quantity, amount);
            if (!tryResult) {
                throw new RuntimeException("Try阶段失败");
            }
            
            // 2. Confirm阶段（模拟异步确认）
            boolean confirmResult = tccOrderService.confirmCreateOrder(xid, userId, productId, quantity, amount);
            if (!confirmResult) {
                throw new RuntimeException("Confirm阶段失败");
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "订单创建成功（TCC模式）");
            result.put("xid", xid);
            result.put("phase", "Try + Confirm");
            
            log.info("=== TCC事务成功完成，xid={} ===", xid);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("TCC事务失败，执行Cancel，xid={}", xid, e);
            
            // 3. Cancel阶段（回滚）
            try {
                tccOrderService.cancelCreateOrder(xid, userId, productId, quantity, amount);
            } catch (Exception cancelEx) {
                log.error("Cancel阶段也失败了", cancelEx);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "订单创建失败，已回滚: " + e.getMessage());
            result.put("xid", xid);
            result.put("phase", "Cancel");
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * TCC Try阶段测试
     */
    @PostMapping("/try")
    public ResponseEntity<?> tryPhase(
            @RequestParam Long userId,
            @RequestParam Long productId,
            @RequestParam Integer quantity,
            @RequestParam BigDecimal amount) {
        
        String xid = UUID.randomUUID().toString().replace("-", "");
        
        try {
            boolean result = tccOrderService.tryCreateOrder(xid, userId, productId, quantity, amount);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", result);
            response.put("xid", xid);
            response.put("phase", "Try");
            response.put("message", result ? "Try阶段成功" : "Try阶段失败");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("xid", xid);
            response.put("message", "Try阶段异常: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * TCC Confirm阶段测试
     */
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPhase(
            @RequestParam String xid,
            @RequestParam Long userId,
            @RequestParam Long productId,
            @RequestParam Integer quantity,
            @RequestParam BigDecimal amount) {
        
        try {
            boolean result = tccOrderService.confirmCreateOrder(xid, userId, productId, quantity, amount);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", result);
            response.put("xid", xid);
            response.put("phase", "Confirm");
            response.put("message", result ? "Confirm阶段成功" : "Confirm阶段失败");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("xid", xid);
            response.put("message", "Confirm阶段异常: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * TCC Cancel阶段测试
     */
    @PostMapping("/cancel")
    public ResponseEntity<?> cancelPhase(
            @RequestParam String xid,
            @RequestParam Long userId,
            @RequestParam Long productId,
            @RequestParam Integer quantity,
            @RequestParam BigDecimal amount) {
        
        try {
            boolean result = tccOrderService.cancelCreateOrder(xid, userId, productId, quantity, amount);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", result);
            response.put("xid", xid);
            response.put("phase", "Cancel");
            response.put("message", result ? "Cancel阶段成功" : "Cancel阶段失败");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("xid", xid);
            response.put("message", "Cancel阶段异常: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取TCC说明
     */
    @GetMapping("/info")
    public ResponseEntity<?> getTccInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "TCC（Try-Confirm-Cancel）");
        info.put("description", "业务层面的分布式事务解决方案");
        
        Map<String, String> phases = new HashMap<>();
        phases.put("try", "尝试阶段：完成业务检查，预留资源");
        phases.put("confirm", "确认阶段：真正执行业务，使用预留资源");
        phases.put("cancel", "取消阶段：释放预留资源");
        info.put("phases", phases);
        
        info.put("advantages", new String[]{"性能较好", "无全局锁", "并发度高", "资源锁定时间短"});
        info.put("disadvantages", new String[]{"业务侵入性强", "开发成本高", "需要处理幂等性", "需要处理空回滚和悬挂"});
        info.put("applicableScenarios", new String[]{"对性能要求高的场景", "并发量大的互联网应用", "需要强一致性的金融业务"});
        
        return ResponseEntity.ok(info);
    }
}
