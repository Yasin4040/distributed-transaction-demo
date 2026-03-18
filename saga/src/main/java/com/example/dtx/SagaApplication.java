package com.example.dtx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Saga模式演示应用
 * 
 * Saga分布式事务（状态机引擎+补偿机制）
 * 
 * 启动前请确保：
 * 1. MySQL已安装并启动
 * 2. 创建了数据库：dtx_saga
 * 3. 执行resources/sql/init.sql创建表
 */
@SpringBootApplication
@EnableAsync
public class SagaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SagaApplication.class, args);
        System.out.println("=== Saga分布式事务演示服务已启动 ===");
        System.out.println("访问 http://localhost:8083/api/saga/info 查看说明");
        System.out.println("访问 http://localhost:8083/api/saga/order 创建订单");
    }
}
