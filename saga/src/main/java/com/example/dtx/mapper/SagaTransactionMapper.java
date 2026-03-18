package com.example.dtx.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.dtx.entity.SagaTransaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Saga事务Mapper
 */
@Mapper
public interface SagaTransactionMapper extends BaseMapper<SagaTransaction> {
    
    /**
     * 查询需要恢复的事务
     */
    @Select("SELECT * FROM t_saga_transaction WHERE status = 0")
    List<SagaTransaction> selectPendingTransactions();
    
    /**
     * 根据业务ID查询
     */
    @Select("SELECT * FROM t_saga_transaction WHERE business_id = #{businessId}")
    SagaTransaction selectByBusinessId(@Param("businessId") String businessId);
}
