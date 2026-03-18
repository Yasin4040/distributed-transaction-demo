package com.example.dtx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * RocketMQ事务消息演示应用
 */
@SpringBootApplication
public class RocketMQApplication {

    public static void main(String[] args) {
        SpringApplication.run(RocketMQApplication.class, args);
        System.out.println("=== RocketMQ事务消息演示服务已启动 ===");
        System.out.println("访问 http://localhost:8085/api/rocketmq/info 查看说明");
        System.out.println("访问 http://localhost:8085/api/rocketmq/order 创建订单");
    }
}
