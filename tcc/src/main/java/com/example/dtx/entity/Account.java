package com.example.dtx.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户实体类
 */
@Data
@TableName("t_account")
public class Account {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String accountNo;
    
    private BigDecimal balance;
    
    private BigDecimal frozenAmount;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
