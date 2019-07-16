package com.example.feng.demo.dao;

import com.atzy.materiel.service.model.ItemInfoBO;
import com.example.feng.demo.model.po.OrderPO;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * @author Kevin Xiao
 * @version 1.0
 * @Description TODO
 * @since 2019/7/1
 */
public interface ItemDAO extends MongoRepository<ItemInfoBO, Integer> {
    ItemInfoBO getByItemId(Integer itemId);
    List<ItemInfoBO> findByItemIdIn(List<Integer> ids);
}
