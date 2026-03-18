package com.example.dtx.mapper.order;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.dtx.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 订单Mapper
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    
    /**
     * 更新订单状态
     */
    @Update("UPDATE t_order SET status = #{status}, update_time = NOW() WHERE order_no = #{orderNo}")
    int updateStatus(@Param("orderNo") String orderNo, @Param("status") Integer status);
}
