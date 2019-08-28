package com.example.feng.demo.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.example.feng.demo.connection.JedisSentinelMasterConnectionFactory;
import com.example.feng.demo.connection.JedisSentinelSlaveConnectionFactory;

import redis.clients.jedis.JedisPoolConfig;

/**
 * @author Kevin Xiao
 * @version 1.0
 * @Description TODO
 * @since 2019/7/24
 */
//@Configuration
public class SlaveReadRedisConfig {
    @Value("${spring.redis.timeout}")
    private int redisTimeout;

    @Value("${spring.redis.connection.timeout:2000}")
    private int connectionTimeout;

    @Value("${spring.redis.database}")
    private int redisDb;

    @Value("${spring.redis.jedis.pool.max-active}")
    private int maxActive;

    @Value("${spring.redis.jedis.pool.max-wait}")
    private int maxWait;

    @Value("${spring.redis.jedis.pool.max-idle}")
    private int maxIdle;

    @Value("${spring.redis.jedis.pool.min-idle}")
    private int minIdle;

    @Value("${spring.redis.sentinel.nodes}")
    private String sentinelNodes;

    @Value("${spring.redis.sentinel.master}")
    private String master;

    private RedisConnectionFactory connectionSlaveFactory() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(maxActive);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMaxWaitMillis(maxWait);
        poolConfig.setMinIdle(minIdle);
        JedisClientConfiguration clientConfig = JedisClientConfiguration.builder().usePooling().poolConfig(poolConfig)
            .and().readTimeout(Duration.ofMillis(redisTimeout)).connectTimeout(Duration.ofMillis(connectionTimeout)).build();

        // 哨兵redis
        RedisSentinelConfiguration redisConfig = new RedisSentinelConfiguration();
        redisConfig.setDatabase(redisDb);
        redisConfig.setMaster(master);
        String[] sentinels = sentinelNodes.split(",");
        for (String sentinel : sentinels) {
            String[] sentinelArray = sentinel.split(":");
            RedisNode node = new RedisNode(sentinelArray[0], Integer.valueOf(sentinelArray[1]));
            redisConfig.sentinel(node);
        }

        JedisSentinelMasterConnectionFactory connectionFactory =
            new JedisSentinelMasterConnectionFactory(redisConfig, clientConfig);
        /*JedisSentinelSlaveConnectionFactory connectionFactory =
                new JedisSentinelSlaveConnectionFactory(redisConfig, clientConfig);*/
        connectionFactory.afterPropertiesSet();
        return connectionFactory;
    }

    @Bean(name = "slaveReadRedisTemplate")
    public RedisTemplate slaveReadRedisTemplate() {
        RedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionSlaveFactory());
        return template;
    }
}
