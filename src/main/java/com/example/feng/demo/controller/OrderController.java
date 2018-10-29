package com.example.feng.demo.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.feng.demo.model.po.OrderPO;
import com.example.feng.demo.service.OrderService;

@RestController
public class OrderController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private OrderService orderService;

    @RequestMapping("/save")
    public String save() {

        for (int i = 4; i < 5; i++) {
            OrderPO order = new OrderPO();
            order.setId(i + 1);
            order.setNickName("小张" + i);
            order.setOrderNo("X79746" + i);
            order.setTotalPrice(4500.00 + i);

            List<Integer> pids = new ArrayList<Integer>();
            /*pids.add(1);
            pids.add(2);
            pids.add(3);*/
            pids.add(11);
            order.setProductIds(pids);

            orderService.saveOrder(order);
        }

        return "ok";
    }

    @RequestMapping("/getByOrderNo")
    public OrderPO getByOrderNo() {

        OrderPO order = orderService.getByOrderNo("X797468");
        return order;
    }

    @RequestMapping("/getByName")
    public OrderPO getByName() {

        OrderPO order = orderService.getByOrderNameLike("张");
        return order;
    }

    @RequestMapping("/getByProductIdsLike")
    public List<OrderPO> getByProductIdsLike() {

        List<OrderPO> order = orderService.getByProductIdsLike(11);
        return order;
    }
}
