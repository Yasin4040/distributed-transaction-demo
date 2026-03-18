package com.example.dtx.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Saga事务记录
 */
@Data
@TableName("t_saga_transaction")
public class SagaTransaction {
    
    @TableId(type = IdType.INPUT)
    private String sagaId;
    
    private String businessType;
    
    private String businessId;
    
    /**
     * 0 - 进行中
     * 1 - 成功
     * 2 - 失败（已补偿）
     * 3 - 部分失败（补偿中）
     */
    private Integer status;
    
    private String currentStep;
    
    private String executedSteps;
    
    private String params;
    
    private String errorMsg;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    private LocalDateTime endTime;
}
