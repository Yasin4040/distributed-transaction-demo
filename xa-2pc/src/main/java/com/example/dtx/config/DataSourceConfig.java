package com.example.dtx.config;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.spring.AtomikosDataSourceBean;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.mysql.cj.jdbc.MysqlXADataSource;
import org.apache.ibatis.session.SqlSessionFactory;
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
 * XA两阶段提交原理：
 * 阶段一（投票阶段）：
 * 1. 协调者向所有参与者发送事务内容，询问是否可以提交
 * 2. 参与者执行本地事务，写入redo和undo日志，锁定资源
 * 3. 参与者返回Yes或No给协调者
 * 
 * 阶段二（提交阶段）：
 * 1. 如果所有参与者返回Yes，协调者发送Commit指令
 * 2. 参与者收到Commit后，执行本地提交，释放锁
 * 3. 如果有参与者返回No，协调者发送Rollback指令
 * 4. 参与者收到Rollback后，执行本地回滚，释放锁
 */
@Configuration
@MapperScan(basePackages = "com.example.dtx.mapper", sqlSessionFactoryRef = "sqlSessionFactory")
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
        props.setProperty("password", "root");
        return props;
    }

    /**
     * 配置MyBatis SqlSessionFactory
     * 使用动态数据源路由
     */
    @Bean(name = "sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(
            @Qualifier("dynamicDataSource") DataSource dynamicDataSource) throws Exception {
        MybatisSqlSessionFactoryBean sessionFactory = new MybatisSqlSessionFactoryBean();
        sessionFactory.setDataSource(dynamicDataSource);
        
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setLogImpl(org.apache.ibatis.logging.stdout.StdOutImpl.class);
        sessionFactory.setConfiguration(configuration);
        
        return sessionFactory.getObject();
    }

    /**
     * 动态数据源
     */
    @Bean(name = "dynamicDataSource")
    public DataSource dynamicDataSource(
            @Qualifier("orderDataSource") DataSource orderDataSource,
            @Qualifier("inventoryDataSource") DataSource inventoryDataSource,
            @Qualifier("accountDataSource") DataSource accountDataSource) {
        
        DynamicRoutingDataSource dynamicDataSource = new DynamicRoutingDataSource();
        dynamicDataSource.setDefaultTargetDataSource(orderDataSource);
        
        java.util.Map<Object, Object> targetDataSources = new java.util.HashMap<>();
        targetDataSources.put("order", orderDataSource);
        targetDataSources.put("inventory", inventoryDataSource);
        targetDataSources.put("account", accountDataSource);
        dynamicDataSource.setTargetDataSources(targetDataSources);
        
        return dynamicDataSource;
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
