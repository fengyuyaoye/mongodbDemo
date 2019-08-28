package com.example.feng.demo.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;

import io.lettuce.core.ReadFrom;
import io.lettuce.core.models.role.RedisInstance;
import io.lettuce.core.models.role.RedisNodeDescription;

/**
 * @author Kevin Xiao
 * @version 1.0
 * @Description TODO
 * @since 2019/7/17
 */
// @Configuration
public class RedisConfig {
    @Value("${spring.redis.read.pool.max-active}")
    private int redisPoolMaxActive;

    @Value("${spring.redis.read.pool.max-wait}")
    private int redisPoolMaxWait;

    @Value("${spring.redis.read.pool.max-idle}")
    private int redisPoolMaxIdle;

    @Value("${spring.redis.read.pool.min-idle}")
    private int redisPoolMinIdle;

//    @Value("${spring.redis.sentinel.nodes}")
    private String sentinelNodes;

    @Value("${spring.redis.sentinel.master}")
    private String master;

    /**
     * 创建redis连接工厂
     */
    protected JedisConnectionFactory createJedisConnectionFactory(int dbIndex, String host, int port, int timeout) {
        GenericObjectPoolConfig poolConfig =
            setPoolConfig(redisPoolMaxIdle, redisPoolMinIdle, redisPoolMaxActive, redisPoolMaxWait, true);
        JedisClientConfiguration clientConfig = JedisClientConfiguration.builder().usePooling().poolConfig(poolConfig)
            .and().readTimeout(Duration.ofMillis(timeout)).build();
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
        /*RedisSentinelConfiguration redisConfig = new RedisSentinelConfiguration();
        redisConfig.setMaster(master);
        redisConfig.setDatabase(dbIndex);
        String[] sentinels = sentinelNodes.split(",");
        for (String sentinel : sentinels) {
            String[] sentinelArray = sentinel.split(":");
            RedisNode node = new RedisNode(sentinelArray[0], Integer.valueOf(sentinelArray[1]));
            redisConfig.sentinel(node);
        }*/

        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setDatabase(dbIndex);
        redisConfig.setHostName(host);
        redisConfig.setPort(port);

        GenericObjectPoolConfig poolConfig =
            setPoolConfig(redisPoolMaxIdle, redisPoolMinIdle, redisPoolMaxActive, redisPoolMaxWait, true);
        LettuceClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder().readFrom(new ReadFrom() {
            @Override
            public List<RedisNodeDescription> select(Nodes nodes) {
                List<RedisNodeDescription> result = new ArrayList();

                List<RedisNodeDescription> allNodes = nodes.getNodes();
                for (int i = 0; i < allNodes.size(); i++) {
                    RedisNodeDescription node = allNodes.get(i);
                    if (node.getRole() == RedisInstance.Role.SLAVE) {
                        result.add(node);
                    }
                }

                Collections.shuffle(result);
                return result;
            }
        }).commandTimeout(Duration.ofMillis(timeout)).poolConfig(poolConfig).build();

        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(redisConfig, clientConfig);
        connectionFactory.afterPropertiesSet();
        connectionFactory.setShareNativeConnection(false);
        return connectionFactory;
    }


    /**
     * 设置连接池属性
     */
    public GenericObjectPoolConfig setPoolConfig(int maxIdle, int minIdle, int maxActive, int maxWait,
        boolean testOnBorrow) {
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(minIdle);
        poolConfig.setMaxTotal(maxActive);
        poolConfig.setMaxWaitMillis(maxWait);
        poolConfig.setTestOnBorrow(testOnBorrow);
        return poolConfig;
    }
}
