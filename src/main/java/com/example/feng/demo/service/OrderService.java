package com.example.feng.demo.service;

import java.util.List;

import com.example.feng.demo.model.po.OrderPO;

public interface OrderService {
    OrderPO getByOrderNo(String orderNo);

    OrderPO getByOrderNoLike(String orderNo);

    OrderPO getByOrderNameLike(String orderName);

    List<OrderPO> getByProductIdsLike(Integer productId);

    void saveOrder(OrderPO order);

    void removeOrderByOrderNo(String orderNo);

    void updateOrder(OrderPO order);

}
