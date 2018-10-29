package com.example.feng.demo.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.example.feng.demo.dao.OrderDao;
import com.example.feng.demo.model.po.OrderPO;
import com.example.feng.demo.service.OrderService;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private OrderDao orderDao;

    @Override
    public OrderPO getByOrderNo(String orderNo) {
        return orderDao.getByOrderNo(orderNo);
    }

    @Override
    public OrderPO getByOrderNoLike(String orderNo) {
        return orderDao.getByOrderNoLike(orderNo);
    }

    @Override
    public void saveOrder(OrderPO order) {
        orderDao.save(order);

    }

    @Override
    public void removeOrderByOrderNo(String orderNo) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateOrder(OrderPO order) {
        // TODO Auto-generated method stub

    }

    @Override
    public OrderPO getByOrderNameLike(String orderName) {
        return orderDao.getByNickNameLike(orderName);
    }

    @Override
    public List<OrderPO> getByProductIdsLike(Integer productId) {
        return orderDao.getByProductIdsLike(productId);
    }

}
