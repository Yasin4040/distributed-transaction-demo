package com.example.dtx.tcc.context;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * TCC事务上下文
 * 
 * 保存TCC事务的执行状态和参数
 */
@Data
public class TccContext {
    
    /**
     * 事务ID
     */
    private String xid;
    
    /**
     * 事务状态
     * 1 - TRYING
     * 2 - CONFIRMING
     * 3 - CANCELLING
     * 4 - CONFIRMED
     * 5 - CANCELLED
     */
    private Integer status;
    
    /**
     * 业务类型
     */
    private String businessType;
    
    /**
     * 业务ID
     */
    private String businessId;
    
    /**
     * 参数
     */
    private Map<String, Object> params = new HashMap<>();
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 重试次数
     */
    private Integer retryCount = 0;
    
    public enum Status {
        TRYING(1, "尝试中"),
        CONFIRMING(2, "确认中"),
        CANCELLING(3, "取消中"),
        CONFIRMED(4, "已确认"),
        CANCELLED(5, "已取消");
        
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
