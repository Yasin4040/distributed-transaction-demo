package com.example.dtx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * TCC模式演示应用
 * 
 * TCC（Try-Confirm-Cancel）分布式事务
 * 
 * 启动前请确保：
 * 1. MySQL已安装并启动
 * 2. 创建了三个数据库：dtx_order, dtx_inventory, dtx_account
 * 3. 执行resources/sql/init.sql创建表
 */
@SpringBootApplication
@EnableScheduling
public class TccApplication {

    public static void main(String[] args) {
        SpringApplication.run(TccApplication.class, args);
        System.out.println("=== TCC分布式事务演示服务已启动 ===");
        System.out.println("访问 http://localhost:8082/api/tcc/info 查看说明");
        System.out.println("访问 http://localhost:8082/api/tcc/order 创建订单");
    }
}
