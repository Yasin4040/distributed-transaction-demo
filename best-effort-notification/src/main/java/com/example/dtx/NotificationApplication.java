package com.example.dtx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 最大努力通知演示应用
 */
@SpringBootApplication
@EnableScheduling
public class NotificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationApplication.class, args);
        System.out.println("=== 最大努力通知演示服务已启动 ===");
        System.out.println("访问 http://localhost:8086/api/notification/info 查看说明");
    }
}
