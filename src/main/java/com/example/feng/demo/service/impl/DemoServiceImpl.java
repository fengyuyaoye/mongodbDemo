package com.example.feng.demo.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.example.feng.demo.controller.bean.People;
import com.example.feng.demo.service.IDemoService;

@Component
public class DemoServiceImpl implements IDemoService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void demo(People people) {
        for (int i = 0; i < 100; i++) {
            if (i % 10 == 0) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                logger.info("name: " + people.getName() + " is busy.");
            }

        }
    }

}
