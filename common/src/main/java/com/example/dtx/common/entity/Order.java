package com.example.dtx.common.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 */
@Data
public class Order {
    
    private Long id;
    
    private String orderNo;
    
    private Long userId;
    
    private Long productId;
    
    private Integer quantity;
    
    private BigDecimal amount;
    
    private Integer status;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    // 订单状态枚举
    public enum Status {
        PENDING(0, "待处理"),
        SUCCESS(1, "成功"),
        FAILED(2, "失败"),
        CANCELLED(3, "已取消");
        
        private final int code;
        private final String desc;
        
        Status(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public int getCode() {
            return code;
        }
        
        public String getDesc() {
            return desc;
        }
    }
}
