package com.example.feng.demo.service.impl;

import com.alibaba.fastjson.JSON;
import com.atzy.materiel.service.model.ItemInfoBO;
import com.example.feng.demo.DemoApplication;
import com.example.feng.demo.service.MaterielService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DemoApplication.class)
@ContextConfiguration(locations = {"classpath:spring.xml"})
public class MaterielServiceImplTest {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private MaterielService materielService;
    @Test
    public void testMongoTemplate() {
        Query qry = new Query();
        qry.addCriteria(Criteria.where("itemId").is(1));
        qry.fields().include("name");
        qry.fields().include("version");
        ItemInfoBO items = mongoTemplate.findOne(qry, ItemInfoBO.class);
        System.out.println("items:" + JSON.toJSONString(items));
    }

    @Test
    public void testGetItemInfoBO() {
        ItemInfoBO item = materielService.getItemInfoBO(1);
        System.out.println("ItemInfoBO: " + JSON.toJSONString(item));
    }
}
