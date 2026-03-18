package com.example.dtx.job;

import com.example.dtx.entity.LocalMessage;
import com.example.dtx.service.LocalMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 消息发送定时任务
 * 
 * 定时扫描待发送消息并发送
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageSenderJob {

    private final LocalMessageService messageService;

    /**
     * 每5秒扫描一次待发送消息
     */
    @Scheduled(fixedRate = 5000)
    public void scanAndSendMessages() {
        log.debug("=== 扫描待发送消息 ===");
        
        List<LocalMessage> pendingMessages = messageService.getPendingMessages();
        
        if (pendingMessages.isEmpty()) {
            log.debug("没有待发送消息");
            return;
        }
        
        log.info("发现 {} 条待发送消息", pendingMessages.size());
        
        for (LocalMessage message : pendingMessages) {
            try {
                // 检查重试次数
                if (message.getRetryCount() >= message.getMaxRetryCount()) {
                    log.warn("消息超过最大重试次数: messageId={}", message.getMessageId());
                    continue;
                }
                
                messageService.sendMessage(message);
            } catch (Exception e) {
                log.error("发送消息失败: messageId={}", message.getMessageId(), e);
            }
        }
    }
}
