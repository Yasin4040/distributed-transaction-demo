package com.example.dtx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Seata TCC模式演示应用
 */
@SpringBootApplication
public class SeataTccApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeataTccApplication.class, args);
        System.out.println("=== Seata TCC模式演示服务已启动 ===");
        System.out.println("访问 http://localhost:8088/api/seata-tcc/info 查看说明");
    }
}
