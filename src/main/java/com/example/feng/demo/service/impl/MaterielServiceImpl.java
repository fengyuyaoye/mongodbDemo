package com.example.feng.demo.service.impl;

import com.atzy.materiel.service.album.IAlbumInfoCacheService;
import com.atzy.materiel.service.artist.IArtistInfoCacheService;
import com.atzy.materiel.service.item.IItemInfoCacheService;
import com.atzy.materiel.service.model.AlbumInfoBO;
import com.atzy.materiel.service.model.ArtistInfoBO;
import com.atzy.materiel.service.model.ItemInfoBO;
import com.example.feng.demo.dao.AlbumDAO;
import com.example.feng.demo.dao.ItemDAO;
import com.example.feng.demo.service.MaterielService;
import com.example.feng.demo.service.MongoService;
import com.example.feng.demo.service.RedisService;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kevin Xiao
 * @version 1.0
 * @Description TODO
 * @since 2019/7/1
 */
@Service
public class MaterielServiceImpl implements MaterielService {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private AlbumDAO albumDAO;
    @Autowired
    private ItemDAO itemDAO;
    @Autowired
    private MongoService mongoService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private IItemInfoCacheService itemInfoCacheService;
    @Autowired
    private IAlbumInfoCacheService albumInfoCacheService;
    @Autowired
    private IArtistInfoCacheService artistInfoCacheService;

    @Override
    public ItemInfoBO getItemInfoBO(int id) {
        List<Integer> ids = new ArrayList<>();
        ids.add(id);
        ItemInfoBO item = findOneByIds(ids, ItemInfoBO.class);

        if (item == null) {
            item = itemInfoCacheService.getRecordFromCache(id);
            mongoService.save(item);
        }
        return item;
    }

    @Override
    public List<ItemInfoBO> getItemInfoBOList(List<Integer> ids, int cnt) {
        List<ItemInfoBO> itemBOList = findAllByIds(ids, ItemInfoBO.class, cnt);
//        List<ItemInfoBO> itemBOList = itemDAO.findByItemIdIn(ids);
        if (CollectionUtils.isEmpty(itemBOList) || itemBOList.size() != ids.size()) {
            List<Integer> savedItemIds = new ArrayList<>();
            for (ItemInfoBO item : itemBOList) {
                savedItemIds.add(item.getItemId());
            }
            List<Integer> unsavedList = ListUtils.removeAll(ids, savedItemIds);
            for (Integer unsaved : unsavedList) {
                ItemInfoBO itemInfoBO = itemInfoCacheService.getRecordFromCache(unsaved);
                mongoService.save(itemInfoBO);
                itemBOList.add(itemInfoBO);
            }
        }
        return itemBOList;
    }

    @Override
    public List<ItemInfoBO> getItemInfoBOListFromRedis(List<Integer> ids) {
        List<ItemInfoBO> itemInfoBOList = redisService.getListByIds(ids, ItemInfoBO.class);
        if (CollectionUtils.isEmpty(itemInfoBOList) || itemInfoBOList.size() != ids.size()) {
            List<Integer> savedItemIds = new ArrayList<>();
            for (ItemInfoBO item : itemInfoBOList) {
                savedItemIds.add(item.getItemId());
            }
            List<Integer> unsavedList = ListUtils.removeAll(ids, savedItemIds);
            for (Integer unsaved : unsavedList) {
                ItemInfoBO item = itemInfoCacheService.getRecordFromCache(unsaved);
                redisService.addRecord(unsaved, item);
                itemInfoBOList.add(item);
                logger.info("item[id={}] load to redis.", unsaved);
            }
        }
        return itemInfoBOList;
    }

    @Override
    public AlbumInfoBO getAlbumInfoBO(int id) {
        /*List<Integer> ids = new ArrayList<>();
        ids.add(id);
        AlbumInfoBO album = findOneByIds(ids, AlbumInfoBO.class);*/
        AlbumInfoBO album = albumDAO.getByAlbumId(id);
        if (album == null) {
            album = albumInfoCacheService.getRecordFromCache(id);
            mongoService.save(album);
        }
        return album;
    }

    @Override
    public AlbumInfoBO getAlbumInfoBOFromRedis(int id) {
        List<Integer> ids = new ArrayList<>();
        ids.add(id);
        List<AlbumInfoBO> albumInfoBOList = redisService.getListByIds(ids, AlbumInfoBO.class);
        if (CollectionUtils.isNotEmpty(albumInfoBOList)) {
            return albumInfoBOList.get(0);
        } else {
            AlbumInfoBO result = albumInfoCacheService.getRecordFromCache(id);
            redisService.addRecord(id, result);
            return result;
        }
    }

    @Override
    public ArtistInfoBO getArtistInfoBO(int id) {
        List<Integer> ids = new ArrayList<>();
        ids.add(id);
        ArtistInfoBO artist = findOneByIds(ids, ArtistInfoBO.class);
        if (artist == null) {
            artist = artistInfoCacheService.getRecordFromCache(id);
            mongoService.save(artist);
        }
        return artist;
    }

    private <T> T findOneByIds(List<Integer> ids, Class<T> clazz) {
        List<T> list = mongoService.findByIds(ids, clazz);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    private <T> List<T> findAllByIds(List<Integer> ids, Class<T> clazz, int fieldCnt) {
        List<T> result = new ArrayList<>();
        List<T> saved = mongoService.findByIds(ids, clazz, fieldCnt);
        if (CollectionUtils.isNotEmpty(saved)) {
            result.addAll(saved);
        }

        return result;
    }
}
