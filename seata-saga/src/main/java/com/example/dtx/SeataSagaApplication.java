package com.example.dtx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Seata Saga模式演示应用
 */
@SpringBootApplication
public class SeataSagaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeataSagaApplication.class, args);
        System.out.println("=== Seata Saga模式演示服务已启动 ===");
        System.out.println("访问 http://localhost:8089/api/seata-saga/info 查看说明");
    }
}
