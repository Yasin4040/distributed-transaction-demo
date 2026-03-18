package com.example.dtx.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * 账户数据源 Mapper 扫描配置
 */
@Configuration
@MapperScan(
    basePackages = "com.example.dtx.mapper.account",
    sqlSessionFactoryRef = "accountSqlSessionFactory"
)
public class AccountDataSourceConfig {
}
