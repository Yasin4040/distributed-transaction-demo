package com.example.dtx.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.dtx.entity.Inventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 库存Mapper
 */
@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {
    
    /**
     * 预留库存（可用库存减少，预留库存增加）
     */
    @Update("UPDATE t_inventory SET available_stock = available_stock - #{quantity}, " +
            "reserved_stock = reserved_stock + #{quantity}, update_time = NOW() " +
            "WHERE product_id = #{productId} AND available_stock >= #{quantity}")
    int reserveStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
    
    /**
     * 释放库存（可用库存增加，预留库存减少）
     */
    @Update("UPDATE t_inventory SET available_stock = available_stock + #{quantity}, " +
            "reserved_stock = reserved_stock - #{quantity}, update_time = NOW() " +
            "WHERE product_id = #{productId}")
    int releaseStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
    
    /**
     * 确认扣减（预留库存减少，总库存减少）
     */
    @Update("UPDATE t_inventory SET reserved_stock = reserved_stock - #{quantity}, " +
            "total_stock = total_stock - #{quantity}, update_time = NOW() " +
            "WHERE product_id = #{productId}")
    int confirmDeduct(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}
