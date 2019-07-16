package com.example.feng.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource(locations = {"classpath*:materiel-service.xml", "classpath*:spring.xml"})
public class Config {

}
