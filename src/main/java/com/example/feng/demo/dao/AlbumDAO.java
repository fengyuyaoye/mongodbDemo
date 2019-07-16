package com.example.feng.demo.dao;

import com.atzy.materiel.service.model.AlbumInfoBO;
import com.example.feng.demo.model.po.OrderPO;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author Kevin Xiao
 * @version 1.0
 * @Description TODO
 * @since 2019/7/1
 */
public interface AlbumDAO extends MongoRepository<AlbumInfoBO, Integer> {
    AlbumInfoBO getByAlbumId(Integer albumId);
}
