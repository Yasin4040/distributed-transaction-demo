package com.example.dtx.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.dtx.entity.TccTransactionLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TCC事务日志Mapper
 */
@Mapper
public interface TccTransactionLogMapper extends BaseMapper<TccTransactionLog> {
    
    /**
     * 查询需要恢复的事务
     */
    @Select("SELECT * FROM t_tcc_transaction_log WHERE status IN (1, 2, 3) AND expire_time < #{now}")
    List<TccTransactionLog> selectExpiredTransactions(@Param("now") LocalDateTime now);
    
    /**
     * 更新事务状态
     */
    @Update("UPDATE t_tcc_transaction_log SET status = #{status}, update_time = NOW(), " +
            "retry_count = retry_count + 1 WHERE xid = #{xid}")
    int updateStatus(@Param("xid") String xid, @Param("status") Integer status);
}
