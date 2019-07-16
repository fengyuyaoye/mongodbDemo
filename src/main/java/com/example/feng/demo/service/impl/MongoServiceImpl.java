package com.example.feng.demo.service.impl;

import com.atzy.materiel.service.model.AlbumInfoBO;
import com.atzy.materiel.service.model.ArtistInfoBO;
import com.atzy.materiel.service.model.ItemInfoBO;
import com.example.feng.demo.service.MongoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author Kevin Xiao
 * @version 1.0
 * @Description TODO
 * @since 2019/7/1
 */
@Service
public class MongoServiceImpl implements MongoService {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void save(Object obj) {
        mongoTemplate.insert(obj);
    }

    @Override
    public <T> List<T> findByIds(List<Integer> ids, Class<T> clazz) {
        return findByIds(ids, clazz, 1000);
    }

    @Override
    public <T> List<T> findByIds(List<Integer> ids, Class<T> clazz, int fieldCnt) {
        Query qry = new Query();
        if (clazz == ItemInfoBO.class) {
            qry.addCriteria(Criteria.where("itemId").in(ids));
            Field[] fields = ItemInfoBO.class.getDeclaredFields();
            if (fieldCnt < fields.length) {
                int cnt = 0;
                for (Field f : fields) {
                    cnt++;
                    /*if ("relAlbums".equals(f.getName()) || "relArtists".equals(f.getName())) {
                        continue;
                    }*/
                    if (cnt < fieldCnt) {
                        qry.fields().include(f.getName());
                    }
                }
            }
        }
        if (clazz == AlbumInfoBO.class) {
            qry.addCriteria(Criteria.where("albumId").in(ids));
        }
        if (clazz == ArtistInfoBO.class) {
            qry.addCriteria(Criteria.where("artistId").in(ids));
        }
        /*qry.fields().include("statusId");
        qry.fields().include("name");*/
        return mongoTemplate.find(qry, clazz);
    }


}
