package com.hesung.openapi.developer.config.datasource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
@MapperScan(
        basePackages = "com.hesung.openapi.developer.dao",
        markerInterface = ManagementDataSourceMapper.class,
        sqlSessionFactoryRef = "managementSqlSessionFactory",
        sqlSessionTemplateRef = "managementSqlSessionTemplate"
)
public class ManagementDataSourceConfig {

    @Primary
    @Bean("managementDataSourceProperties")
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSourceProperties managementDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean("managementDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public DataSource managementDataSource(
            @Qualifier("managementDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Primary
    @Bean("managementSqlSessionFactory")
    public SqlSessionFactory managementSqlSessionFactory(
            @Qualifier("managementDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setTypeAliasesPackage("com.hesung.openapi.developer.dao.entity");
        return factoryBean.getObject();
    }

    @Primary
    @Bean("managementSqlSessionTemplate")
    public SqlSessionTemplate managementSqlSessionTemplate(
            @Qualifier("managementSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Primary
    @Bean("managementTransactionManager")
    public DataSourceTransactionManager managementTransactionManager(
            @Qualifier("managementDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
