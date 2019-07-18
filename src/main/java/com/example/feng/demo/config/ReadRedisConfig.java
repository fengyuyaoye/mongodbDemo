package com.example.feng.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author Kevin Xiao
 * @version 1.0
 * @Description TODO
 * @since 2019/7/17
 */
@Configuration
public class ReadRedisConfig extends RedisConfig {


    @Value("${spring.redis.read.database}")
    private int dbIndex;

    @Value("${spring.redis.read.host}")
    private String host;

    @Value("${spring.redis.read.port}")
    private int port;

    @Value("${spring.redis.read.timeout}")
    private int timeout;

    /**
     * 配置redis连接工厂
     */
    private RedisConnectionFactory defaultRedisConnectionFactory() {
        return createJedisConnectionFactory(dbIndex, host, port, timeout);
    }

    private RedisConnectionFactory lettuceConnectionFactory() {
        return createLettuceConnectionFactory(dbIndex, host, port, timeout);
    }

    /**
     * 配置redisTemplate 注入方式使用@Resource(name="") 方式注入
     */
    @Bean(name = "readRedisTemplate")
    public RedisTemplate defaultRedisTemplate() {
        RedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(lettuceConnectionFactory());
        return template;
    }
}
