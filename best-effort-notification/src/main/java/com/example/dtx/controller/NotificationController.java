package com.example.dtx.controller;

import com.example.dtx.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 最大努力通知控制器
 */
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(
            @RequestParam String businessId,
            @RequestParam String content) {
        
        boolean success = notificationService.sendNotification(businessId, content);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "通知已发送" : "通知发送失败");
        
        return ResponseEntity.ok(result);
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmNotification(@RequestParam String businessId) {
        notificationService.confirmNotification(businessId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "确认成功");
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/info")
    public ResponseEntity<?> getInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "最大努力通知");
        info.put("description", "通过定时重试保证通知的可靠性");
        info.put("advantages", new String[]{"实现简单", "对业务侵入小"});
        info.put("disadvantages", new String[]{"不保证一定成功", "有延迟"});
        info.put("applicableScenarios", new String[]{"支付回调", "短信通知", "邮件通知"});
        
        return ResponseEntity.ok(info);
    }
}
