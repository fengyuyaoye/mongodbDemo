package com.example.feng.demo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.example.feng.demo.controller.bean.People;
import com.example.feng.demo.service.IDemoService;

@RestController
public class LoggerController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private IDemoService demoService;

    @RequestMapping("/hello")
    public String hello(People people) {

        logger.info("LoggerController.hello param: " + JSON.toJSONString(people));
        demoService.demo(people);

        DemoVO result = new DemoVO();
        result.setRespCode(0);
        result.setRespMsg("success");
        logger.info("LoggerController.hello result: " + JSON.toJSONString(result));

        return JSON.toJSONString(result);
    }

}
