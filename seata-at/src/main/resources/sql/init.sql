-- ============================================
-- Seata AT模式演示 - 数据库初始化脚本
-- ============================================

CREATE DATABASE IF NOT EXISTS dtx_seata_at CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE dtx_seata_at;

-- 订单表
CREATE TABLE IF NOT EXISTS t_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(64) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    status TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seata undo_log表（必须）
CREATE TABLE IF NOT EXISTS undo_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    branch_id BIGINT NOT NULL COMMENT '分支事务ID',
    xid VARCHAR(128) NOT NULL COMMENT '全局事务ID',
    context VARCHAR(128) NOT NULL COMMENT '上下文',
    rollback_info LONGBLOB NOT NULL COMMENT '回滚信息',
    log_status TINYINT NOT NULL COMMENT '状态：0-正常 1-全局已完成',
    log_created DATETIME NOT NULL COMMENT '创建时间',
    log_modified DATETIME NOT NULL COMMENT '修改时间',
    UNIQUE INDEX ux_undo_log (xid, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata AT模式undo_log表';
