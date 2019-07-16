package com.example.feng.demo.config;

import com.mongodb.MongoClientOptions;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Kevin Xiao
 * @version 1.0
 * @Description TODO
 * @since 2019/7/4
 */
//@Configuration
public class MongoConfig {
//    @Bean
    public MongoClientOptions mongoOptions() {
        return MongoClientOptions.builder().maxConnectionIdleTime(6000).build();
    }
}
