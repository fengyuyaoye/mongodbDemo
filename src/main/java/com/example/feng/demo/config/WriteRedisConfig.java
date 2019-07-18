package com.example.feng.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

import redis.clients.jedis.JedisPoolConfig;

/**
 * @author Kevin Xiao
 * @version 1.0
 * @Description TODO
 * @since 2019/7/17
 */
@Configuration
public class WriteRedisConfig {
    /*@Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;*/

    @Value("${spring.redis.timeout}")
    private int redisTimeout;

    /*@Value("${spring.redis.password}")
    private String redisAuth;*/

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

    public RedisConnectionFactory connectionFactory() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(maxActive);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMaxWaitMillis(maxWait);
        poolConfig.setMinIdle(minIdle);
        JedisClientConfiguration clientConfig = JedisClientConfiguration.builder()
                .usePooling().poolConfig(poolConfig).and().readTimeout(Duration.ofMillis(redisTimeout)).build();

        // 单点redis
//        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        // 哨兵redis
        RedisSentinelConfiguration redisConfig = new RedisSentinelConfiguration();
        redisConfig.setMaster("mymaster");
        String[] sentinels = sentinelNodes.split(",");
        for (String sentinel : sentinels) {
            String[] sentinelArray = sentinel.split(":");
            RedisNode node = new RedisNode(sentinelArray[0], Integer.valueOf(sentinelArray[1]));
            redisConfig.sentinel(node);
        }

        // 集群redis
        // RedisClusterConfiguration redisConfig = new RedisClusterConfiguration();
        /*redisConfig.setHostName(redisHost);
        redisConfig.setPassword(RedisPassword.of(redisAuth));
        redisConfig.setPort(redisPort);*/
        redisConfig.setDatabase(redisDb);

        JedisConnectionFactory connectionFactory = new JedisConnectionFactory(redisConfig, clientConfig);
        connectionFactory.afterPropertiesSet();
        return connectionFactory;
    }

    @Bean(name = "writeRedisTemplate")
    public RedisTemplate writeRedisTemplate() {
        RedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory());
        return template;
    }
}
