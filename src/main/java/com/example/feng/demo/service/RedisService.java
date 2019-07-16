package com.example.feng.demo.service;

import java.util.List;

/**
 * @author Kevin Xiao
 * @version 1.0
 * @Description TODO
 * @since 2019/7/10
 */
public interface RedisService {

     <T> List<T> getListByIds(List<Integer> ids, Class<T> clazz);

     <T> void addRecord(Integer id, T record);
}
