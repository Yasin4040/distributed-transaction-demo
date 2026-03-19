# 分布式事务解决方案完整演示项目

本项目展示了 **9 种主流分布式事务解决方案**，每个方案都是独立的 Spring Boot 模块，包含完整的示例代码和测试接口。无论是学习分布式事务原理，还是在实际项目中选型，都能从中获得参考。

---

## 项目结构

```
distributed-transaction-demo/
├── pom.xml                          # 父POM
├── docker-compose.yml               # 依赖服务Docker配置
├── README.md                        # 项目说明（本文件）
├── common/                          # 公共模块
├── xa-2pc/                          # XA两阶段提交
├── tcc/                             # TCC模式
├── saga/                            # Saga模式
├── local-message/                   # 本地消息表
├── rocketmq-message/                # RocketMQ事务消息
├── best-effort-notification/        # 最大努力通知
├── seata-at/                        # Seata AT模式
├── seata-tcc/                       # Seata TCC模式
└── seata-saga/                      # Seata Saga模式
```

---

## 方案概览

| 方案 | 端口 | 核心原理 | 一致性 | 难度 | 推荐场景 |
|------|------|----------|--------|------|----------|
| XA/2PC | 8081 | 协调者两阶段提交 | 强一致 | **极高** | 仅入门学习、强一致+低并发 |
| TCC | 8082 | Try-Confirm-Cancel | 最终一致 | 高 | 高并发、金融 |
| Saga | 8083 | 拆分事务+补偿 | 最终一致 | 中 | 微服务、长流程 |
| 本地消息表 | 8084 | 消息表+定时任务 | 最终一致 | 中 | 异步场景 |
| RocketMQ消息 | 8085 | 半消息+本地事务 | 最终一致 | 中 | 已有RocketMQ |
| 最大努力通知 | 8086 | 重试机制 | 最终一致 | 低 | 通知类场景 |
| Seata AT | 8087 | 自动undo_log | 最终一致 | 低 | 无侵入、快速改造 |
| Seata TCC | 8088 | 框架管理TCC | 最终一致 | 高 | 标准化TCC |
| Seata Saga | 8089 | 状态机编排 | 最终一致 | 中 | 可视化编排 |

---

## 核心概念

在开始学习之前，需要理解以下核心概念：

### 1. 强一致性 vs 最终一致性

| 特性 | 强一致性 | 最终一致性 |
|------|----------|------------|
| 定义 | 事务提交后，所有参与者数据立即一致 | 允许短暂不一致，但最终会一致 |
| 性能 | 较低（同步阻塞） | 较高（异步执行） |
| 方案 | XA/2PC | TCC、Saga、消息队列 |
| 适用 | 金融核心系统 | 电商订单、普通业务 |

### 2. 幂等性 (Idempotency)

同一个操作执行多次，结果相同。补偿操作、消息消费必须考虑幂等性。

```java
// 示例：扣款接口的幂等处理
@PostMapping("/deduct")
public Result deduct(@RequestParam String orderId, @RequestParam BigDecimal amount) {
    // 先查询是否已处理
    if (orderMapper.existsByOrderId(orderId)) {
        return Result.success("已处理");
    }
    // 处理业务
    accountMapper.deduct(amount);
    orderMapper.save(orderId);
    return Result.success("扣款成功");
}
```

### 3. 空回滚 (空补偿)

Try 阶段未执行时收到 Cancel 请求，需要正常返回，不能报错。

```java
@LocalTCCCancel(cancelMethod = "cancel")
public void cancel(String orderId) {
    // 先检查Try是否已执行
    Order order = orderMapper.findByOrderId(orderId);
    if (order == null || order.getStatus().equals("CANCELLED")) {
        // 空回滚，直接返回
        return;
    }
    // 执行真正的取消逻辑
    order.setStatus("CANCELLED");
    orderMapper.update(order);
}
```

### 4. 悬挂

Cancel 先到，Try 后到，需要防止资源无法释放。

```java
@LocalTCCTry(cancelMethod = "cancel")
public void try_(String orderId) {
    // 先检查是否已有取消记录（防止悬挂）
    if (cancelLogMapper.existsByOrderId(orderId)) {
        throw new RuntimeException("订单已取消，不能执行Try");
    }
    // 执行Try逻辑
    orderMapper.save(Order.of(orderId, "PENDING"));
}
```

---

## 学习路径建议

### 第一阶段：理解基础概念

```
推荐：XA/2PC → 本地消息表
```

- **XA/2PC** (端口 8081)
  - 最基础的分布式事务方案
  - 理解"协调者"和"参与者"的概念
  - 优点：强一致性、理论成熟、对业务侵入小
  - 缺点：同步阻塞、性能差、单点故障
  - 适合：低并发、短事务、入门学习

- **本地消息表** (端口 8084)
  - 理解"最终一致性"的核心思想
  - 学习如何用数据库消息表保证一致性
  - 优点：实现简单、无外部依赖
  - 缺点：需要维护消息表、定时轮询有延迟
  - 适合：异步场景、支付回调

### 第二阶段：进阶方案

```
推荐：TCC → Saga → 最大努力通知
```

- **TCC** (端口 8082)
  - Try-Confirm-Cancel 三阶段模式
  - 需要自己编写 Try/Confirm/Cancel 逻辑
  - 关键难点：幂等性、空回滚、悬挂
  - 优点：无全局锁、并发度高、性能好
  - 缺点：业务侵入性强、开发成本高
  - 适合：高并发、金融场景

- **Saga** (端口 8083)
  - 将大事务拆成小事务 + 补偿
  - 无全局锁，长事务友好
  - 优点：适合微服务架构、异步执行
  - 缺点：补偿逻辑复杂、隔离性问题
  - 适合：长流程、多服务调用

- **最大努力通知** (端口 8086)
  - 最简单的方案，重试几次后放弃
  - 优点：实现简单、对业务侵入小
  - 缺点：不保证一定成功
  - 适合：短信通知、邮件通知、支付回调

### 第三阶段：使用框架

```
推荐：Seata AT → Seata TCC → Seata Saga
```

- **Seata AT** (端口 8087)
  - 对业务无侵入，零代码改动
  - 自动生成 undo_log 自动回滚
  - 优点：接入简单、性能好
  - 缺点：需要 Seata Server、不支持复杂 SQL
  - **推荐生产使用**

- **Seata TCC** (端口 8088)
  - 框架管理的 TCC
  - 提供幂等控制、空回滚支持
  - 适合：需要 TCC 但想用框架管理

- **Seata Saga** (端口 8089)
  - 基于 JSON 状态机定义
  - 可视化编排
  - 适合：复杂业务流程

### 第四阶段：消息队列方案

```
RocketMQ 事务消息 (端口 8085)
```

- 需要 RocketMQ 环境
- 半消息机制
- 适合：已有 RocketMQ 的项目

---

## 场景推荐

| 你的场景 | 推荐方案 | 理由 |
|----------|----------|------|
| 入门学习分布式事务原理 | XA/2PC | 概念简单，但配置复杂，仅适合学习 |
| 入门学习分布式事务实践 | 本地消息表 | 实现简单，容易理解 |
| 金融/支付系统 | TCC、Seata TCC | 强一致性、高可靠性 |
| 微服务长流程 | Saga、Seata Saga | 无全局锁、适合拆分 |
| 现有项目快速改造 | Seata AT | 零代码改动、无侵入 |
| 支付回调/通知 | 最大努力通知、本地消息表 | 实现简单、适合异步 |
| 已有 RocketMQ | RocketMQ 事务消息 | 复用现有基础设施 |
| 追求高性能 | TCC、Saga | 无全局锁、异步执行 |

---

## 环境要求

### 基础环境
- JDK 17+
- Maven 3.8+
- MySQL 8.0+

### 特定方案需要
- **RocketMQ 事务消息**: 需要 RocketMQ 5.0+ (NameServer + Broker)
- **Seata 系列**: 需要 Seata Server 1.8+ (端口 8091)

### 快速启动依赖

使用 docker-compose 快速启动所有依赖：

```bash
# 启动 MySQL、RocketMQ、Seata 等
docker-compose up -d

# 查看运行状态
docker-compose ps
```

---

## 快速开始

### 1. 初始化数据库

执行各模块下的 `src/main/resources/sql/init.sql` 文件创建数据库和表：

```bash
# 方式一：手动执行SQL
mysql -u root -p < xa-2pc/src/main/resources/sql/init.sql
mysql -u root -p < tcc/src/main/resources/sql/init.sql
# ... 其他模块同理

# 方式二：启动后自动创建（如果配置了DDL权限）
```

### 2. 编译项目

```bash
mvn clean install -DskipTests
```

### 3. 启动模块

```bash
# XA两阶段提交
cd xa-2pc && mvn spring-boot:run

# TCC模式（新开终端）
cd tcc && mvn spring-boot:run

# ... 其他模块同理
```

### 4. 测试接口

以 XA/2PC 为例（其他模块类似，把端口和路径换成对应的）：

```bash
# 查看说明
curl http://localhost:8081/api/xa/info

# 创建订单（成功场景）
curl -X POST "http://localhost:8081/api/xa/order?userId=1&productId=1&quantity=1&amount=100"

# 创建订单（失败场景，测试回滚）
curl -X POST "http://localhost:8081/api/xa/order/failure?userId=1&productId=1&quantity=1&amount=100"
```

---

## 各模块详细说明

### 1. XA两阶段提交 (xa-2pc)

**端口**: 8081

**原理**:
- 阶段一（投票）：协调者询问所有参与者是否可以提交
- 阶段二（提交）：所有参与者返回 Yes 则提交，任一返回 No 则回滚

**⚠️ 配置复杂度高**：XA 2PC 需要为**每个数据源**独立配置：
- `AtomikosDataSourceBean`（XA 数据源包装器）
- `SqlSessionFactory`（MyBatis 会话工厂）
- `MapperScan`（指定 Mapper 扫描路径）

原因：XA 要求每个数据库连接都是独立的 XA 资源，参与全局事务协调。MyBatis 需要知道每个 Mapper 使用哪个数据源，无法使用动态数据源切换。

**回滚机制实现**：
```
业务异常 → Spring 捕获 → JtaTransactionManager.rollback() 
    → Atomikos 协调器 → XAResource.rollback() 
    → 各数据库执行本地回滚
```
- 一阶段：所有参与者执行本地 SQL，但不提交，记录 redo/undo 日志
- 二阶段（回滚）：协调者发送 Rollback 指令，各参与者根据 undo 日志回滚
- 回滚是**自动触发**的：业务代码抛出异常 → Spring 事务切面捕获 → 调用 JTA 回滚

**优点**: 强一致性、理论成熟、对业务侵入小

**缺点**: 
- **配置极其复杂**（多数据源需多套配置）
- 同步阻塞、性能较差、单点故障、资源锁定时间长
- 不适合高并发场景

**适用场景**: 仅推荐用于**入门学习**或**强一致且低并发**的金融核心场景

**测试接口**:
```bash
curl http://localhost:8081/api/xa/info
curl -X POST "http://localhost:8081/api/xa/order?userId=1&productId=1&quantity=1&amount=100"
curl -X POST "http://localhost:8081/api/xa/order/failure?userId=1&productId=1&quantity=1&amount=100"
```

---

### 2. TCC模式 (tcc)

**端口**: 8082

**原理**:
- Try：完成业务检查，预留资源
- Confirm：真正执行业务，使用预留资源
- Cancel：释放预留资源

**优点**: 性能好、无全局锁、并发度高

**缺点**: 业务侵入性强、需要处理幂等性/空回滚/悬挂

**测试接口**:
```bash
curl http://localhost:8082/api/tcc/info
# 完整TCC流程
curl -X POST "http://localhost:8082/api/tcc/order?userId=1&productId=1&quantity=1&amount=100"
# 单独测试各阶段
curl -X POST "http://localhost:8082/api/tcc/try?userId=1&productId=1&quantity=1&amount=100"
curl -X POST "http://localhost:8082/api/tcc/confirm?xid=xxx&userId=1&productId=1&quantity=1&amount=100"
curl -X POST "http://localhost:8082/api/tcc/cancel?xid=xxx&userId=1&productId=1&quantity=1&amount=100"
```

---

### 3. Saga模式 (saga)

**端口**: 8083

**原理**:
- 将大事务拆分为多个本地事务
- 每个本地事务有对应的补偿操作
- 执行失败时反向执行补偿

**优点**: 长事务支持、无全局锁、适合微服务

**缺点**: 最终一致性、补偿逻辑复杂、隔离性问题

**测试接口**:
```bash
curl http://localhost:8083/api/saga/info
curl -X POST "http://localhost:8083/api/saga/order?userId=1&productId=1&quantity=1&amount=100"
```

---

### 4. 本地消息表 (local-message)

**端口**: 8084

**原理**:
- 业务操作和消息记录在同一个本地事务
- 定时任务扫描待发送消息
- 消息发送成功后更新状态

**优点**: 实现简单、无外部依赖、消息可靠

**缺点**: 需要维护消息表、定时轮询有延迟

**测试接口**:
```bash
curl http://localhost:8084/api/local-message/info
curl -X POST "http://localhost:8084/api/local-message/order?userId=1&productId=1&quantity=1&amount=100"
```

---

### 5. RocketMQ事务消息 (rocketmq-message)

**端口**: 8085

**原理**:
- 发送半消息（Half Message）
- 执行本地事务
- 根据结果提交或回滚半消息
- 消费方消费消息执行业务

**前置条件**: 启动 RocketMQ NameServer 和 Broker

**测试接口**:
```bash
curl http://localhost:8085/api/rocketmq/info
curl -X POST "http://localhost:8085/api/rocketmq/order?userId=1&productId=1&quantity=1&amount=100"
```

---

### 6. 最大努力通知 (best-effort-notification)

**端口**: 8086

**原理**:
- 本地事务成功后发送消息
- 定时任务重试
- 达到最大次数后放弃

**适用场景**: 支付回调、短信通知、邮件通知

**测试接口**:
```bash
curl http://localhost:8086/api/notification/info
curl -X POST "http://localhost:8086/api/notification/send?businessId=123&content=test"
curl -X POST "http://localhost:8086/api/notification/confirm?businessId=123"
```

---

### 7. Seata AT模式 (seata-at)

**端口**: 8087

**原理**:
- 一阶段：业务SQL直接执行，生成前后镜像，写入undo_log
- 二阶段-成功：异步删除undo_log
- 二阶段-失败：根据undo_log回滚数据

**前置条件**: 启动 Seata Server

**测试接口**:
```bash
curl http://localhost:8087/api/seata-at/info
curl -X POST "http://localhost:8087/api/seata-at/order?userId=1&productId=1&quantity=1&amount=100"
```

---

### 8. Seata TCC模式 (seata-tcc)

**端口**: 8088

**原理**:
- Seata 对 TCC 的标准化实现
- 使用注解标记 Try/Confirm/Cancel
- Seata 协调器统一管理

**前置条件**: 启动 Seata Server

**测试接口**:
```bash
curl http://localhost:8088/api/seata-tcc/info
curl -X POST "http://localhost:8088/api/seata-tcc/order?userId=1&productId=1&quantity=1&amount=100"
```

---

### 9. Seata Saga模式 (seata-saga)

**端口**: 8089

**原理**:
- 基于 JSON 状态机定义
- 可视化编排
- 自动补偿

**前置条件**: 启动 Seata Server

**测试接口**:
```bash
curl http://localhost:8089/api/seata-saga/info
curl -X POST "http://localhost:8089/api/seata-saga/order?userId=1&productId=1&quantity=1&amount=100"
```

---

## 方案选型决策树

```
开始
  │
  ├─ 是否需要强一致性？
  │   └─ 是 → XA/2PC（仅适合低并发短事务）
  │
  └─ 否（最终一致性）→
        │
        ├─ 是否已有 RocketMQ？
        │   └─ 是 → RocketMQ 事务消息
        │
        ├─ 是否已有 Seata？
        │   └─ 是 → Seata AT（推荐）或 Seata TCC
        │
        ├─ 是否高并发金融场景？
        │   └─ 是 → TCC（需要技术能力强）
        │
        ├─ 是否微服务长流程？
        │   └─ 是 → Saga
        │
        ├─ 是否通知类场景？
        │   └─ 是 → 最大努力通知
        │
        └─ 其他 → 本地消息表 或 Seata AT
```

---

## 生产实践建议

### 1. 方案选择

| 场景 | 推荐方案 | 理由 |
|------|----------|------|
| 中小项目 | Seata AT | 接入简单、零代码改动 |
| 金融/支付 | TCC / Seata TCC | 高可靠性、强一致性 |
| 微服务 | Saga / Seata Saga | 适合长流程、拆分服务 |
| 异步通知 | 本地消息表 / RocketMQ | 消息可靠投递 |

### 2. 注意事项

1. **幂等性**: 所有补偿操作和消息消费都必须保证幂等性
2. **空回滚**: TCC 模式需要处理 Try 未执行时的 Cancel 请求
3. **悬挂**: TCC 模式需要处理 Try 晚于 Cancel 到达的情况
4. **监控**: 生产环境需要完善的监控和告警
5. **人工介入**: 部分极端情况可能需要人工介入处理
6. **测试**: 充分测试各种异常场景（网络超时、服务宕机、数据超时等）

### 3. 最佳实践

- **优先考虑最终一致性**：强一致性方案性能差，大部分业务场景最终一致性足够
- **业务层处理补偿**：不要完全依赖框架，自己编写补偿逻辑更可控
- **做好监控告警**：分布式事务失败是常态，需要及时发现和处理
- **设计回滚方案**：提前设计好人工回滚预案

---

## 总结

这个项目是分布式事务的"百科全书"：
2PC 是算法/协议（Two-Phase Commit，两阶段提交的思想）
XA 是接口规范（X/Open 组织把 2PC 算法标准化后的工业标准，定义了 TM 和 RM 的交互接口）

XA更像是一种悲观锁，对于分布式事务的一种悲观实现。
悲观并发控制（PCC）的定义：先假设冲突会发生，所以先加锁，再操作，最后释放。

XA 悲观：先锁资源，再做事（怕别人抢，先占着）
TCC 乐观：先做事（预留），再补锁（假设冲突少，真冲突了再处理）

| 类别 | 方案 | 特点 |
|------|------|------|
| 理论基础 | XA/2PC | 理解分布式事务的起点 |
| 简单实用 | 本地消息表、最大努力通知 | 实现简单，适合异步场景 |
| 高性能 | TCC、Saga | 无全局锁，适合高并发 |
| 框架化 | Seata AT/TCC/Saga | 开箱即用，生态完善 |
| 消息队列 | RocketMQ 事务消息 | 复用现有 MQ |

**实际生产推荐**：
- 中小项目 → **Seata AT**
- 金融/高并发 → **TCC / Seata TCC**
- 微服务长流程 → **Saga / Seata Saga**



SEATA
https://seata.apache.org/zh-cn/docs/dev/mode/tcc-mode

根据两阶段行为模式的不同，我们将分支事务划分为 Automatic (Branch) Transaction Mode 和 TCC (Branch) Transaction Mode.

AT 模式基于 支持本地 ACID 事务 的 关系型数据库：

一阶段 prepare 行为：在本地事务中，一并提交业务数据更新和相应回滚日志记录。
二阶段 commit 行为：马上成功结束，自动 异步批量清理回滚日志。
二阶段 rollback 行为：通过回滚日志，自动 生成补偿操作，完成数据回滚。
相应的，TCC 模式，不依赖于底层数据资源的事务支持：

一阶段 prepare 行为：调用 自定义 的 prepare 逻辑。
二阶段 commit 行为：调用 自定义 的 commit 逻辑。
二阶段 rollback 行为：调用 自定义 的 rollback 逻辑。
所谓 TCC 模式，是指支持把 自定义 的分支事务纳入到全局事务的管理中。




| 阶级          | 你的定位                          | 技术人格特征                                                                                                                        | 固化原因（为什么爬不出去）                                                                               |
| ----------- | ----------------------------- | ----------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------- |
| **最底层**     | **XA/2PC**<br>**平凡人**         | **被体制（数据库）完全锁死的流水线工人**<br>数据库说"准备"，他锁表；说"提交"，他点头。<br>没有自己的积蓄（无业务冻结），没有技能（无三阶段接口），**纯靠组织（数据库）分配资源**。                           | **出身即原罪**：一旦选型 XA，就被数据库连接池和行锁钉死在车间，想翻身（改 TCC）要推翻整个车间重建（换架构），成本极高。                           |
| **好一点阶级**   | **TCC**<br>**小镇做题家/技术中产**     | **有专业技能（状态机），有积蓄（冻结字段），精打细算**<br>自己写代码管资源（不依赖数据库锁），但活得累——每次交易要记三本账（Try/Confirm/Cancel）。<br>怕生病（宕机），怕失业（网络分区），因为家底（冻结资源）可能被悬挂。 | **技能陷阱**：升上去（改 Saga）要放弃严谨性，降下去（改 XA）要丢掉技能，卡在中间**不敢消费**（Confirm前不敢真动），只能苦哈哈预留。               |
| **统治阶级/富豪** | **Saga**<br>**富二代/富豪**        | **家里有信托基金（补偿逻辑），敢直接消费（直接扣库存）**<br>不怕试错，因为错了能 restoration（恢复）。<br>脏读（别人看到他花钱了）是他的特权——**我敢承担暂时的不一致，因为我有资本兜底**。                  | **资源世袭**：敢用 Saga 的前提是家里有矿（补偿服务能力），普通人（无补偿逻辑）硬装富豪，超卖了就是**暴雷（P0事故）**。                         |
| **普通打工人**   | **RocketMQ**<br>**按时打卡的社畜**   | **只管自己 KPI（本地事务提交），不管下游死活**<br>"我消息发出去了（打卡下班），你爱消费不消费（你爱干不干）"。<br>反正月底对账（定时任务）能平就行，平不了让运营（HR）去撕逼。                             | **螺丝钉化**：只能做单一环节（生产消息），无法掌控全局（不协调下游），**向上爬（改 TCC）要背三阶段黑锅，向下沉（改最大努力）要丢饭碗（可靠性）**，卡在中间混日子。     |
| **超脱者**     | **最大努力通知**<br>**努力的道士/佛系修行者** | **念经（定时重试），化缘（HTTP 通知），看缘分（无回执）**<br>"我努力过了（重试 5 次），施主（下游系统）收不到是福报不够"。<br>无状态（无事务表），无积蓄（无冻结），**彻底躺平，只靠信念（定时任务）驱动**。           | **出世即入世**：看似最自由（无状态机束缚），实则最卑微——**连本地事务都可能没有**，像街头卖艺，给多少看天意，**想回凡间（改 RocketMQ）要先考编（补事务消息）**。 |

---

## 贡献指南

欢迎提交 Issue 和 PR！

## 许可证

MIT License
