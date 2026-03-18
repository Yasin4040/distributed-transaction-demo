package com.example.dtx.common.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户实体类
 */
@Data
public class Account {
    
    private Long id;
    
    private Long userId;
    
    private String accountNo;
    
    private BigDecimal balance;
    
    private BigDecimal frozenAmount;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
