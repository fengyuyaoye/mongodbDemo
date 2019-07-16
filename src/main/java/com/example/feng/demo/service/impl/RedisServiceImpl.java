package com.example.feng.demo.service.impl;

import com.alibaba.fastjson.JSON;
import com.atzy.materiel.util.CacheUtil;
import com.example.feng.demo.service.RedisService;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kevin Xiao
 * @version 1.0
 * @Description TODO
 * @since 2019/7/10
 */
@Service
public class RedisServiceImpl implements RedisService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public <T> List<T> getListByIds(List<Integer> ids, Class<T> clazz) {
        List<String> keys = new ArrayList<>();
        for (Integer id : ids) {
            String key = CacheUtil.generateCacheKey(id, clazz, 1);
            keys.add(key);
        }

        List<T> result = new ArrayList<>();
        List<String> recordList = redisTemplate.opsForValue().multiGet(keys);
        for (String record : recordList) {
            if (StringUtils.isNotBlank(record)) {
                result.add(JSON.parseObject(record, clazz));
            }
        }
        return result;
    }

    @Override
    public <T> void addRecord(Integer id, T record) {
        String key = CacheUtil.generateCacheKey(id, record.getClass(), 1);
        redisTemplate.opsForValue().set(key, JSON.toJSONString(record));
        logger.info("add record[id={}, class={}] success.", id, record.getClass().getName());
    }
}
