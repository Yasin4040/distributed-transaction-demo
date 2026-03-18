package com.example.dtx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * XA两阶段提交演示应用
 * 
 * 启动前请确保：
 * 1. MySQL已安装并启动
 * 2. 创建了三个数据库：dtx_order, dtx_inventory, dtx_account
 * 3. 执行resources/sql/init.sql创建表
 */
@SpringBootApplication
@EnableTransactionManagement
public class XA2PCApplication {

    public static void main(String[] args) {
        SpringApplication.run(XA2PCApplication.class, args);
        System.out.println("=== XA两阶段提交演示服务已启动 ===");
        System.out.println("访问 http://localhost:8081/api/xa/info 查看说明");
        System.out.println("访问 http://localhost:8081/api/xa/order 创建订单");
    }
}
