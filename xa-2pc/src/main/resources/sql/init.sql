-- ============================================
-- XA两阶段提交演示 - 数据库初始化脚本
-- ============================================

-- 创建订单数据库
CREATE DATABASE IF NOT EXISTS dtx_order CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建库存数据库
CREATE DATABASE IF NOT EXISTS dtx_inventory CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建账户数据库
CREATE DATABASE IF NOT EXISTS dtx_account CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用订单数据库
USE dtx_order;

-- 订单表
CREATE TABLE IF NOT EXISTS t_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    order_no VARCHAR(64) NOT NULL UNIQUE COMMENT '订单编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    quantity INT NOT NULL COMMENT '购买数量',
    amount DECIMAL(10, 2) NOT NULL COMMENT '订单金额',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待处理 1-成功 2-失败 3-已取消',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 使用库存数据库
USE dtx_inventory;

-- 库存表
CREATE TABLE IF NOT EXISTS t_inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    product_id BIGINT NOT NULL UNIQUE COMMENT '商品ID',
    product_name VARCHAR(128) NOT NULL COMMENT '商品名称',
    total_stock INT DEFAULT 0 COMMENT '总库存',
    available_stock INT DEFAULT 0 COMMENT '可用库存',
    reserved_stock INT DEFAULT 0 COMMENT '预留库存',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表';

-- 初始化库存数据
INSERT INTO t_inventory (product_id, product_name, total_stock, available_stock, reserved_stock) VALUES
(1, 'iPhone 15 Pro', 100, 100, 0),
(2, 'MacBook Pro', 50, 50, 0),
(3, 'AirPods Pro', 200, 200, 0)
ON DUPLICATE KEY UPDATE product_name = VALUES(product_name);

-- 使用账户数据库
USE dtx_account;

-- 账户表
CREATE TABLE IF NOT EXISTS t_account (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    account_no VARCHAR(64) NOT NULL COMMENT '账户编号',
    balance DECIMAL(10, 2) DEFAULT 0.00 COMMENT '余额',
    frozen_amount DECIMAL(10, 2) DEFAULT 0.00 COMMENT '冻结金额',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账户表';

-- 初始化账户数据
INSERT INTO t_account (user_id, account_no, balance, frozen_amount) VALUES
(1, 'ACC202400001', 10000.00, 0.00),
(2, 'ACC202400002', 5000.00, 0.00),
(3, 'ACC202400003', 8000.00, 0.00)
ON DUPLICATE KEY UPDATE account_no = VALUES(account_no);
