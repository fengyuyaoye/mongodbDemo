package com.example.feng.demo.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.example.feng.demo.cache.CustomizeCacheManager;
import com.example.feng.demo.cache.RedisCache;
import com.example.feng.demo.cache.SimpleCache;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Kevin Xiao
 * @version 1.0
 * @Description TODO
 * @since 2019/7/17
 */
@EnableCaching
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

    @Autowired
    private SimpleCache simpleCache;
    @Autowired
    private RedisCache redisCache;

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
        Jackson2JsonRedisSerializer<Object> serializer = new CustomizorJackson2JsonRedisSerializer<Object>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        serializer.setObjectMapper(objectMapper);

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.setConnectionFactory(lettuceConnectionFactory());
        template.afterPropertiesSet();
        return template;
    }

    @Bean(name = "redis1CacheManager")
    public CacheManager cacheManager() {
        CustomizeCacheManager cacheManager = new CustomizeCacheManager();
        simpleCache.setName("memcache");
        redisCache.setName("redis");
        List caches = new ArrayList<>();
        caches.add(simpleCache);
        caches.add(redisCache);
        cacheManager.setCaches(caches);
        return cacheManager;
    }
}
