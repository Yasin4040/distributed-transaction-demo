package com.example.dtx.common.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 库存实体类
 */
@Data
public class Inventory {
    
    private Long id;
    
    private Long productId;
    
    private String productName;
    
    private Integer totalStock;
    
    private Integer availableStock;
    
    private Integer reservedStock;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
