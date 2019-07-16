package com.example.feng.demo.config;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;

import org.apache.commons.lang.ArrayUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = {"com.example.feng.demo.dao", "com.atzy.materiel.dao"},
    sqlSessionFactoryRef = "primarySqlSessionFactory")
public class DataSourceConfig {

    @Primary
    @Bean(name = "primaryDataSource")
    @ConfigurationProperties("spring.datasource.primary")
    public DataSource masterDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "primarySqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("primaryDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
        sessionFactoryBean.setDataSource(dataSource);

        Resource[] resources1 = new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/*.xml");
        Resource[] resources2 =
            new PathMatchingResourcePatternResolver().getResources("classpath*:materielMapper/*.xml");
        Resource[] resources = (Resource[])ArrayUtils.addAll(resources1, resources2);

        sessionFactoryBean.setMapperLocations(resources);
        return sessionFactoryBean.getObject();
    }

}
