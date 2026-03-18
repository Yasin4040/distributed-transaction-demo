package com.example.dtx.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.dtx.entity.LocalMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 本地消息Mapper
 */
@Mapper
public interface LocalMessageMapper extends BaseMapper<LocalMessage> {
    
    /**
     * 查询待发送消息
     */
    @Select("SELECT * FROM t_local_message WHERE status IN (0, 4) AND retry_count < max_retry_count")
    List<LocalMessage> selectPendingMessages();
    
    /**
     * 根据消息ID查询
     */
    @Select("SELECT * FROM t_local_message WHERE message_id = #{messageId}")
    LocalMessage selectByMessageId(@Param("messageId") String messageId);
}
