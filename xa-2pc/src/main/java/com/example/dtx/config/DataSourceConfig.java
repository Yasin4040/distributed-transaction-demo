package com.example.dtx.config;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.spring.AtomikosDataSourceBean;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.mysql.cj.jdbc.MysqlXADataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.sql.DataSource;
import jakarta.transaction.UserTransaction;

/**
 * XA多数据源配置类
 * 
 * 每个数据源独立配置 SqlSessionFactory，配合 JTA 实现分布式事务
 */
@Configuration
public class DataSourceConfig {

    /**
     * 订单数据源
     */
    @Bean(name = "orderDataSource")
    @Primary
    public DataSource orderDataSource() {
        AtomikosDataSourceBean dataSource = new AtomikosDataSourceBean();
        dataSource.setUniqueResourceName("orderDB");
        dataSource.setXaDataSourceClassName(MysqlXADataSource.class.getName());
        dataSource.setXaProperties(getXaProperties("dtx_order"));
        dataSource.setMinPoolSize(5);
        dataSource.setMaxPoolSize(20);
        return dataSource;
    }

    /**
     * 库存数据源
     */
    @Bean(name = "inventoryDataSource")
    public DataSource inventoryDataSource() {
        AtomikosDataSourceBean dataSource = new AtomikosDataSourceBean();
        dataSource.setUniqueResourceName("inventoryDB");
        dataSource.setXaDataSourceClassName(MysqlXADataSource.class.getName());
        dataSource.setXaProperties(getXaProperties("dtx_inventory"));
        dataSource.setMinPoolSize(5);
        dataSource.setMaxPoolSize(20);
        return dataSource;
    }

    /**
     * 账户数据源
     */
    @Bean(name = "accountDataSource")
    public DataSource accountDataSource() {
        AtomikosDataSourceBean dataSource = new AtomikosDataSourceBean();
        dataSource.setUniqueResourceName("accountDB");
        dataSource.setXaDataSourceClassName(MysqlXADataSource.class.getName());
        dataSource.setXaProperties(getXaProperties("dtx_account"));
        dataSource.setMinPoolSize(5);
        dataSource.setMaxPoolSize(20);
        return dataSource;
    }

    private java.util.Properties getXaProperties(String database) {
        java.util.Properties props = new java.util.Properties();
        props.setProperty("url", "jdbc:mysql://localhost:3306/" + database + 
            "?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf-8");
        props.setProperty("user", "root");
        props.setProperty("password", "123456");
        return props;
    }

    // ==================== Order 数据源配置 ====================
    
    @Bean(name = "orderSqlSessionFactory")
    @Primary
    public SqlSessionFactory orderSqlSessionFactory(@Qualifier("orderDataSource") DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean sessionFactory = new MybatisSqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setLogImpl(org.apache.ibatis.logging.stdout.StdOutImpl.class);
        sessionFactory.setConfiguration(configuration);
        
        return sessionFactory.getObject();
    }

    @Bean(name = "orderSqlSessionTemplate")
    @Primary
    public SqlSessionTemplate orderSqlSessionTemplate(@Qualifier("orderSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    // ==================== Inventory 数据源配置 ====================
    
    @Bean(name = "inventorySqlSessionFactory")
    public SqlSessionFactory inventorySqlSessionFactory(@Qualifier("inventoryDataSource") DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean sessionFactory = new MybatisSqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setLogImpl(org.apache.ibatis.logging.stdout.StdOutImpl.class);
        sessionFactory.setConfiguration(configuration);
        
        return sessionFactory.getObject();
    }

    @Bean(name = "inventorySqlSessionTemplate")
    public SqlSessionTemplate inventorySqlSessionTemplate(@Qualifier("inventorySqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    // ==================== Account 数据源配置 ====================
    
    @Bean(name = "accountSqlSessionFactory")
    public SqlSessionFactory accountSqlSessionFactory(@Qualifier("accountDataSource") DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean sessionFactory = new MybatisSqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setLogImpl(org.apache.ibatis.logging.stdout.StdOutImpl.class);
        sessionFactory.setConfiguration(configuration);
        
        return sessionFactory.getObject();
    }

    @Bean(name = "accountSqlSessionTemplate")
    public SqlSessionTemplate accountSqlSessionTemplate(@Qualifier("accountSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    /**
     * JTA事务管理器
     */
    @Bean(name = "jtaTransactionManager")
    public JtaTransactionManager jtaTransactionManager() throws Exception {
        UserTransactionManager userTransactionManager = new UserTransactionManager();
        userTransactionManager.setForceShutdown(true);
        userTransactionManager.init();
        
        UserTransaction userTransaction = new UserTransactionImp();
        userTransaction.setTransactionTimeout(300);
        
        return new JtaTransactionManager(userTransaction, userTransactionManager);
    }
}
