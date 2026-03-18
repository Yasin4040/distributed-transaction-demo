# XA 两阶段提交 (2PC) 模块

## 概述

本模块演示 **XA 两阶段提交**分布式事务方案，使用 **Atomikos** 作为事务协调器，实现跨多个 MySQL 数据库的强一致性事务。

---

## 核心原理

### 两阶段提交流程

```
┌─────────────┐         阶段一（准备）         ┌─────────────┐
│  协调者      │ ◄──────────────────────────► │  参与者      │
│ (Atomikos)  │   1. 发送 Prepare 指令        │ (MySQL XA)  │
│             │   2. 执行本地 SQL（不提交）     │             │
│             │   3. 返回 Yes/No             │             │
└──────┬──────┘                              └─────────────┘
       │
       │ 所有返回 Yes
       ▼
┌─────────────┐         阶段二（提交）         ┌─────────────┐
│  协调者      │ ◄──────────────────────────► │  参与者      │
│ (Atomikos)  │   1. 发送 Commit 指令         │ (MySQL XA)  │
│             │   2. 执行本地提交              │             │
│             │   3. 释放锁资源                │             │
└─────────────┘                              └─────────────┘
```

**阶段一（投票阶段）**：
1. 协调者向所有参与者发送 Prepare 指令
2. 参与者执行本地 SQL，记录 undo/redo 日志，**不提交**
3. 参与者返回 Yes（可以提交）或 No（执行失败）

**阶段二（提交阶段）**：
- 所有参与者返回 Yes → 协调者发送 **Commit** 指令
- 任一参与者返回 No → 协调者发送 **Rollback** 指令

---

## ⚠️ 配置复杂度说明

XA 2PC 需要为**每个数据源**独立配置：

| 配置项 | 数量 | 说明 |
|--------|------|------|
| `AtomikosDataSourceBean` | 3 个 | XA 数据源包装器（订单、库存、账户） |
| `SqlSessionFactory` | 3 个 | MyBatis 会话工厂 |
| `MapperScan` | 3 个 | 指定 Mapper 扫描路径 |
| `SqlSessionTemplate` | 3 个 | MyBatis 会话模板 |

**为什么需要这么多配置？**

XA 要求每个数据库连接都是独立的 **XA 资源**，参与全局事务协调。MyBatis 需要知道每个 Mapper 使用哪个数据源，**无法使用动态数据源切换**。

```
订单服务 ──► orderMapper ──► orderSqlSessionFactory ──► orderDataSource ──► dtx_order
库存服务 ──► inventoryMapper ──► inventorySqlSessionFactory ──► inventoryDataSource ──► dtx_inventory
账户服务 ──► accountMapper ──► accountSqlSessionFactory ──► accountDataSource ──► dtx_account
```

---

## 回滚机制实现

### 触发流程

```
业务异常抛出
    ↓
Spring @Transactional 切面捕获
    ↓
JtaTransactionManager.rollback()
    ↓
Atomikos 事务协调器
    ↓
XAResource.rollback(XID) 发送到各数据库
    ↓
MySQL 根据 undo_log 执行回滚
    ↓
释放锁资源
```

### 关键组件说明

```java
@Bean(name = "jtaTransactionManager")
public JtaTransactionManager jtaTransactionManager() throws Exception {
    // 1. 事务协调者（TM）：负责指挥多个数据库提交/回滚
    UserTransactionManager userTransactionManager = new UserTransactionManager();
    userTransactionManager.setForceShutdown(true);
    userTransactionManager.init();
    
    // 2. 事务接口实现：提供 begin()、commit()、rollback() 给业务调用
    UserTransaction userTransaction = new UserTransactionImp();
    userTransaction.setTransactionTimeout(300);
    
    // 3. 桥接到 Spring：让 @Transactional 能驱动 Atomikos
    return new JtaTransactionManager(userTransaction, userTransactionManager);
}
```

| 组件 | 所属 | 作用 |
|------|------|------|
| `UserTransactionManager` | Atomikos | **事务协调者**（TM），负责决策和指挥 |
| `UserTransactionImp` | Atomikos | **事务接口**，业务代码通过它控制事务 |
| `JtaTransactionManager` | Spring | **桥梁**，让 Spring 的 `@Transactional` 能管理分布式事务 |

### undo_log 机制

**Atomikos 本身不管理 undo 日志**，而是**依赖数据库自身的 undo 机制**：

1. **第一阶段**：MySQL 执行 SQL 但不提交，自动记录 undo_log
   - INSERT 的 undo 是 DELETE
   - UPDATE 的 undo 是 UPDATE 恢复原值

2. **第二阶段（回滚）**：MySQL 读取 undo_log，执行反向操作恢复数据

---

## 数据库准备

### 1. 创建数据库

```sql
-- 订单库
CREATE DATABASE dtx_order DEFAULT CHARACTER SET utf8mb4;

-- 库存库
CREATE DATABASE dtx_inventory DEFAULT CHARACTER SET utf8mb4;

-- 账户库
CREATE DATABASE dtx_account DEFAULT CHARACTER SET utf8mb4;
```

### 2. 创建表

**dtx_order.t_order**
```sql
CREATE TABLE t_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(64) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status TINYINT DEFAULT 0 COMMENT '0-待处理 1-成功 2-失败',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

**dtx_inventory.t_inventory**
```sql
CREATE TABLE t_inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL UNIQUE,
    total_stock INT DEFAULT 0,
    available_stock INT DEFAULT 0,
    reserved_stock INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 初始化库存
INSERT INTO t_inventory (product_id, total_stock, available_stock) VALUES (1, 100, 100);
```

**dtx_account.t_account**
```sql
CREATE TABLE t_account (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    balance DECIMAL(10,2) DEFAULT 0,
    frozen_amount DECIMAL(10,2) DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 初始化账户
INSERT INTO t_account (user_id, balance) VALUES (1, 1000.00);
```

---

## 启动说明

### 1. 配置数据库连接

修改 `src/main/java/com/example/dtx/config/DataSourceConfig.java`：

```java
private java.util.Properties getXaProperties(String database) {
    java.util.Properties props = new java.util.Properties();
    props.setProperty("url", "jdbc:mysql://localhost:3306/" + database + 
        "?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf-8");
    props.setProperty("user", "root");
    props.setProperty("password", "你的密码");  // ← 修改这里
    return props;
}
```

### 2. 编译运行

```bash
# 方式一：命令行
mvn clean spring-boot:run

# 方式二：IDEA
直接运行 XA2PCApplication.java
```

### 3. 验证启动

看到以下日志表示启动成功：
```
Started XA2PCApplication in x.x seconds
AtomikosDataSourceBean initialized
```

---

## 测试接口

### 基础信息

```bash
# 获取 XA 说明
curl http://localhost:8081/api/xa/info
```

### 成功场景

```bash
# 创建订单（正常流程）
curl -X POST "http://localhost:8081/api/xa/order?userId=1&productId=1&quantity=1&amount=100"
```

**预期结果**：
- 订单创建成功（status=1）
- 库存扣减（available_stock-1, reserved_stock+1）
- 账户扣款（balance-100, frozen_amount+100）

### 回滚场景

```bash
# 创建订单（模拟失败，测试回滚）
curl -X POST "http://localhost:8081/api/xa/order/failure?userId=1&productId=1&quantity=1&amount=100"
```

**预期结果**：
- 抛出异常："模拟业务异常，测试XA回滚"
- 订单未创建（或 status=0）
- 库存不变
- 账户余额不变
- 日志显示：`XAResource.rollback` → `rollback() done`

---

## 方案优缺点

### 优点

| 优点 | 说明 |
|------|------|
| 强一致性 | 理论成熟，ACID 特性完整 |
| 对业务侵入小 | 只需 `@Transactional`，业务代码无感知 |
| 标准协议 | XA 是业界标准，主流数据库都支持 |

### 缺点

| 缺点 | 说明 |
|------|------|
| **配置极其复杂** | 多数据源需多套配置，无法使用动态数据源 |
| 同步阻塞 | 二阶段提交期间，资源一直被锁定 |
| 单点故障 | 协调者（Atomikos）宕机可能导致事务悬挂 |
| 性能较差 | 网络往返多，锁持有时间长 |
| 不适合高并发 | 资源锁定导致吞吐量下降 |

---

## 适用场景

| 推荐 | 场景 |
|------|------|
| ✅ | 入门学习分布式事务原理 |
| ✅ | 强一致性 + 低并发 + 短事务（如金融核心对账） |
| ❌ | 高并发电商系统 |
| ❌ | 微服务长流程 |
| ❌ | 追求快速开发的项目 |

---

## 替代方案建议

如果 XA 2PC 配置过于复杂，考虑以下替代方案：

| 方案 | 复杂度 | 一致性 | 适用场景 |
|------|--------|--------|----------|
| **Seata AT** | ⭐⭐ 低 | 最终一致 | 中小项目，无侵入改造 |
| **本地消息表** | ⭐⭐ 低 | 最终一致 | 异步场景，支付回调 |
| **TCC** | ⭐⭐⭐⭐ 高 | 最终一致 | 高并发金融场景 |

---

## 常见问题

### Q1: 为什么不用动态数据源？

XA 要求每个数据源是独立的 XA 资源，动态数据源切换会导致所有操作走默认数据源，无法参与全局事务。

### Q2: 回滚是自动的吗？

是的。业务代码抛异常 → Spring 捕获 → 自动调用 `JtaTransactionManager.rollback()` → Atomikos 协调各数据库回滚。

### Q3: 可以和其他方案混用吗？

不建议。XA 是强一致性方案，与其他最终一致性方案混用可能导致数据不一致。

### Q4: 生产环境推荐吗？

仅在**强一致且低并发**场景推荐。一般业务推荐 Seata AT 或本地消息表。

---

## 参考文档

- [Atomikos 官方文档](https://www.atomikos.com/Main/Documentation)
- [XA 规范](https://pubs.opengroup.org/onlinepubs/009680699/toc.pdf)
- [MySQL XA 事务](https://dev.mysql.com/doc/refman/8.0/en/xa.html)
