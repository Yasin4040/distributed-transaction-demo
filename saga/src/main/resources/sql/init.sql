-- ============================================
-- Saga模式演示 - 数据库初始化脚本
-- ============================================

-- 创建Saga数据库
CREATE DATABASE IF NOT EXISTS dtx_saga CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE dtx_saga;

-- Saga事务记录表
CREATE TABLE IF NOT EXISTS t_saga_transaction (
    saga_id VARCHAR(64) PRIMARY KEY COMMENT 'Saga事务ID',
    business_type VARCHAR(32) NOT NULL COMMENT '业务类型',
    business_id VARCHAR(64) NOT NULL COMMENT '业务ID',
    status TINYINT DEFAULT 0 COMMENT '状态：0-进行中 1-成功 2-失败 3-补偿中',
    current_step VARCHAR(64) COMMENT '当前步骤',
    executed_steps TEXT COMMENT '已执行步骤JSON',
    params TEXT COMMENT '参数JSON',
    error_msg VARCHAR(512) COMMENT '错误信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    end_time DATETIME COMMENT '结束时间',
    INDEX idx_status (status),
    INDEX idx_business_id (business_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Saga事务记录表';
