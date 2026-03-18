package com.example.dtx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 本地消息表演示应用
 */
@SpringBootApplication
@EnableScheduling
public class LocalMessageApplication {

    public static void main(String[] args) {
        SpringApplication.run(LocalMessageApplication.class, args);
        System.out.println("=== 本地消息表模式演示服务已启动 ===");
        System.out.println("访问 http://localhost:8084/api/local-message/info 查看说明");
        System.out.println("访问 http://localhost:8084/api/local-message/order 创建订单");
    }
}
