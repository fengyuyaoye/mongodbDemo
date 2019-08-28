package com.example.feng.demo.config;

import com.fasterxml.jackson.databind.JavaType;

import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.lang.Nullable;

/**
 * @author Kevin Xiao
 * @version 1.0
 * @Description TODO
 * @since 2019/8/8
 */
public class CustomizorJackson2JsonRedisSerializer<T> extends Jackson2JsonRedisSerializer {
    public CustomizorJackson2JsonRedisSerializer(JavaType javaType) {
        super(javaType);
    }
    public CustomizorJackson2JsonRedisSerializer(Class<T> type) {
        super(type);
    }

    public T deserialize(@Nullable byte[] bytes) throws SerializationException {
        T result = null;
        try {
            result = (T)super.deserialize(bytes);
        } catch (Exception e) {

        }
        return result;
    }
}
