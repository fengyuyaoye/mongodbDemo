package com.example.feng.demo.service;

import java.util.List;

/**
 * @author Kevin Xiao
 * @version 1.0
 * @Description TODO
 * @since 2019/7/1
 */
public interface MongoService {
    void save(Object obj);
    <T> List<T> findByIds(List<Integer> ids, Class<T> clazz);
    <T> List<T> findByIds(List<Integer> ids, Class<T> clazz, int fieldCnt);
}
