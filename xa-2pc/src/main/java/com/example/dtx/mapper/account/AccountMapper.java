package com.example.dtx.mapper.account;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.dtx.entity.Account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

/**
 * 账户Mapper
 */
@Mapper
public interface AccountMapper extends BaseMapper<Account> {
    
    /**
     * 扣减余额
     */
    @Update("UPDATE t_account SET balance = balance - #{amount}, " +
            "frozen_amount = frozen_amount + #{amount}, update_time = NOW() " +
            "WHERE user_id = #{userId} AND balance >= #{amount}")
    int deductBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);
    
    /**
     * 释放余额
     */
    @Update("UPDATE t_account SET balance = balance + #{amount}, " +
            "frozen_amount = frozen_amount - #{amount}, update_time = NOW() " +
            "WHERE user_id = #{userId}")
    int releaseBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);
    
    /**
     * 确认扣减
     */
    @Update("UPDATE t_account SET frozen_amount = frozen_amount - #{amount}, update_time = NOW() " +
            "WHERE user_id = #{userId}")
    int confirmDeduct(@Param("userId") Long userId, @Param("amount") BigDecimal amount);
}
