package com.example.feng.demo.config;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;

import java.time.Duration;

/**
 * @author Kevin Xiao
 * @version 1.0
 * @Description TODO
 * @since 2019/7/17
 */
@Configuration
public class RedisConfig {
    @Value("${spring.redis.read.pool.max-active}")
    private int redisPoolMaxActive;

    @Value("${spring.redis.read.pool.max-wait}")
    private int redisPoolMaxWait;

    @Value("${spring.redis.read.pool.max-idle}")
    private int redisPoolMaxIdle;

    @Value("${spring.redis.read.pool.min-idle}")
    private int redisPoolMinIdle;

    /**
     * 创建redis连接工厂
     */
    protected JedisConnectionFactory createJedisConnectionFactory(int dbIndex, String host, int port, int timeout) {
        GenericObjectPoolConfig poolConfig = setPoolConfig(redisPoolMaxIdle, redisPoolMinIdle, redisPoolMaxActive, redisPoolMaxWait, true);
        JedisClientConfiguration clientConfig = JedisClientConfiguration.builder()
                .usePooling().poolConfig(poolConfig).and().readTimeout(Duration.ofMillis(timeout)).build();
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setDatabase(dbIndex);
        redisConfig.setHostName(host);
        redisConfig.setPort(port);
        JedisConnectionFactory connectionFactory = new JedisConnectionFactory(redisConfig, clientConfig);
        connectionFactory.afterPropertiesSet();
        return connectionFactory;
    }

    /**
     * 创建redis连接工厂
     */
    protected LettuceConnectionFactory createLettuceConnectionFactory(int dbIndex, String host, int port, int timeout) {
        GenericObjectPoolConfig poolConfig = setPoolConfig(redisPoolMaxIdle, redisPoolMinIdle, redisPoolMaxActive, redisPoolMaxWait, true);
        LettuceClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .commandTimeout(Duration.ofMillis(timeout)).poolConfig(poolConfig).build();

        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setDatabase(dbIndex);
        redisConfig.setHostName(host);
        redisConfig.setPort(port);
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(redisConfig, clientConfig);
        connectionFactory.afterPropertiesSet();
        return connectionFactory;
    }


    /**
     * 设置连接池属性
     */
    public GenericObjectPoolConfig setPoolConfig(int maxIdle, int minIdle, int maxActive, int maxWait, boolean testOnBorrow) {
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(minIdle);
        poolConfig.setMaxTotal(maxActive);
        poolConfig.setMaxWaitMillis(maxWait);
        poolConfig.setTestOnBorrow(testOnBorrow);
        return poolConfig;
    }
}
