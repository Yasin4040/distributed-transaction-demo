package com.example.dtx.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 本地消息表
 * 
 * 本地消息表模式原理：
 * 1. 业务操作和消息记录在同一个本地事务中
 * 2. 定时任务扫描待发送消息
 * 3. 消息发送成功后更新状态
 * 4. 消费方处理消息后确认
 */
@Data
@TableName("t_local_message")
public class LocalMessage {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 消息ID
     */
    private String messageId;
    
    /**
     * 业务类型
     */
    private String businessType;
    
    /**
     * 业务ID
     */
    private String businessId;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 目标队列/主题
     */
    private String destination;
    
    /**
     * 状态：0-待发送 1-发送中 2-已发送 3-消费成功 4-发送失败
     */
    private Integer status;
    
    /**
     * 重试次数
     */
    private Integer retryCount;
    
    /**
     * 最大重试次数
     */
    private Integer maxRetryCount;
    
    /**
     * 错误信息
     */
    private String errorMsg;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    private LocalDateTime sendTime;
    
    public enum Status {
        PENDING(0, "待发送"),
        SENDING(1, "发送中"),
        SENT(2, "已发送"),
        CONSUMED(3, "消费成功"),
        FAILED(4, "发送失败");
        
        private final int code;
        private final String desc;
        
        Status(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public int getCode() {
            return code;
        }
    }
}
