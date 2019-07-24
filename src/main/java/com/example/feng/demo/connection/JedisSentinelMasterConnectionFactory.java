package com.example.feng.demo.connection;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.util.CollectionUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.util.Pool;

/**
 * @author Kevin Xiao
 * @version 1.0
 * @Description TODO
 * @since 2019/7/24
 */
public class JedisSentinelMasterConnectionFactory extends JedisConnectionFactory {

    public JedisSentinelMasterConnectionFactory(RedisSentinelConfiguration sentinelConfig,
        JedisClientConfiguration clientConfig) {
        super(sentinelConfig, clientConfig);
    }

    protected Pool<Jedis> createRedisSentinelPool(RedisSentinelConfiguration config) {
        GenericObjectPoolConfig poolConfig =
            this.getPoolConfig() != null ? this.getPoolConfig() : new JedisPoolConfig();
        return new JedisSentinelMasterPool(config.getMaster().getName(),
            this.convertToJedisSentinelSet(config.getSentinels()), poolConfig, this.getConnectTimeout(),
            this.getReadTimeout(), this.getPassword(), this.getDatabase(), this.getClientName());
    }

    private int getConnectTimeout() {
        return Math.toIntExact(getClientConfiguration().getConnectTimeout().toMillis());
    }

    private Set<String> convertToJedisSentinelSet(Collection<RedisNode> nodes) {

        if (CollectionUtils.isEmpty(nodes)) {
            return Collections.emptySet();
        }

        Set<String> convertedNodes = new LinkedHashSet<>(nodes.size());
        for (RedisNode node : nodes) {
            if (node != null) {
                convertedNodes.add(node.asString());
            }
        }
        return convertedNodes;
    }

    private int getReadTimeout() {
        return Math.toIntExact(getClientConfiguration().getReadTimeout().toMillis());
    }
}
