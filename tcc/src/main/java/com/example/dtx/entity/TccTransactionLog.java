package com.example.dtx.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * TCC事务日志表
 * 
 * 用于记录TCC事务的执行状态，实现幂等性和恢复
 */
@Data
@TableName("t_tcc_transaction_log")
public class TccTransactionLog {
    
    @TableId(type = IdType.INPUT)
    private String xid;
    
    private String businessType;
    
    private String businessId;
    
    /**
     * 1 - TRYING
     * 2 - CONFIRMING
     * 3 - CANCELLING
     * 4 - CONFIRMED
     * 5 - CANCELLED
     */
    private Integer status;
    
    private String params;
    
    private Integer retryCount;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    private LocalDateTime expireTime;
}
