package com.example.feng.demo.dao;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.feng.demo.model.po.OrderPO;

public interface OrderDao extends MongoRepository<OrderPO, Integer> {
    OrderPO getByOrderNo(String orderNo);

    OrderPO getByOrderNoLike(String orderNo);

    OrderPO getByNickNameLike(String orderName);

    List<OrderPO> getByProductIdsLike(Integer productId);

}
