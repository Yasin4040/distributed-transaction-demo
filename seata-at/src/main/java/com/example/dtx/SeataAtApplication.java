package com.example.dtx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Seata AT模式演示应用
 */
@SpringBootApplication
public class SeataAtApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeataAtApplication.class, args);
        System.out.println("=== Seata AT模式演示服务已启动 ===");
        System.out.println("访问 http://localhost:8087/api/seata-at/info 查看说明");
        System.out.println("访问 http://localhost:8087/api/seata-at/order 创建订单");
    }
}
