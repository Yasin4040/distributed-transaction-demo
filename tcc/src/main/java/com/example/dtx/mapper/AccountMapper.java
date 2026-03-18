package com.example.dtx.mapper;

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
     * 冻结余额（余额减少，冻结金额增加）
     */
    @Update("UPDATE t_account SET balance = balance - #{amount}, " +
            "frozen_amount = frozen_amount + #{amount}, update_time = NOW() " +
            "WHERE user_id = #{userId} AND balance >= #{amount}")
    int freezeBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);
    
    /**
     * 解冻余额（余额增加，冻结金额减少）
     */
    @Update("UPDATE t_account SET balance = balance + #{amount}, " +
            "frozen_amount = frozen_amount - #{amount}, update_time = NOW() " +
            "WHERE user_id = #{userId}")
    int unfreezeBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);
    
    /**
     * 确认扣减（冻结金额减少）
     */
    @Update("UPDATE t_account SET frozen_amount = frozen_amount - #{amount}, update_time = NOW() " +
            "WHERE user_id = #{userId}")
    int confirmDeduct(@Param("userId") Long userId, @Param("amount") BigDecimal amount);
}
