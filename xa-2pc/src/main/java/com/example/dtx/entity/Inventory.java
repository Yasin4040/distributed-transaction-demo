package com.example.dtx.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 库存实体类
 */
@Data
@TableName("t_inventory")
public class Inventory {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long productId;
    
    private String productName;
    
    private Integer totalStock;
    
    private Integer availableStock;
    
    private Integer reservedStock;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
