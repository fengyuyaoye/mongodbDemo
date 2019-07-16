package com.example.feng.demo.service.impl;

import com.alibaba.fastjson.JSON;
import com.atzy.materiel.dao.po.RelatedPartyStatus;
import com.atzy.materiel.service.model.AlbumInfoBO;
import com.atzy.materiel.service.model.ItemInfoBO;
import com.example.feng.demo.dao.IAlbumDAO;
import com.example.feng.demo.dao.IArtistDAO;
import com.example.feng.demo.dao.IContentFileDAO;
import com.example.feng.demo.dao.IItemDAO;
import com.example.feng.demo.service.IMaterielCacheService;
import com.example.feng.demo.service.MaterielService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class MaterielCacheServiceImpl implements IMaterielCacheService {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private IItemDAO itemDAO;
    @Autowired
    private IAlbumDAO albumDAO;
    @Autowired
    private IArtistDAO artistDAO;
    @Autowired
    private IContentFileDAO contentFileDAO;
    @Autowired
    private MaterielService materielService;

    @Override
    public void cacheItemInfo(int maxItemId) {
        int maxId = itemDAO.selectMaxItemId();
        int totalNum = maxItemId > maxId ? maxId : maxItemId;
        int totalIdsPerPage = 1000;
        int totalPage = totalNum / totalIdsPerPage + 1;

        int min = 0;
        int max = 0;
        long begin = System.currentTimeMillis();

        for (int i = 0; i < totalPage; i++) {
            max = (i + 1) * totalIdsPerPage;
            max = max > totalNum ? totalNum : max;
            List<Integer> itemIds = itemDAO.selectItemIds(min, max);
            logger.info("itemId[" + min + ", " + max + "]: " + JSON.toJSONString(itemIds));
            min = max;
            for (Integer itemId : itemIds) {
                materielService.getItemInfoBO(itemId);
            }
        }
        long end = System.currentTimeMillis();
        logger.info("cacheItem total time: " + (end - begin) / 1000.0);

    }

    @Override
    public void cacheAlbumInfo(int maxAlbumId) {
        int maxId = albumDAO.selectMaxAlbumId();
        int totalNum = maxAlbumId > maxId ? maxId : maxAlbumId;
        int totalIdsPerPage = 1000;
        int totalPage = totalNum / totalIdsPerPage + 1;

        int min = 0;
        int max = 0;
        long begin = System.currentTimeMillis();

        for (int i = 0; i < totalPage; i++) {
            max = (i + 1) * totalIdsPerPage;
            max = max > totalNum ? totalNum : max;
            List<Integer> albumIds = albumDAO.selectAlbumIds(min, max);
            logger.info("itemId[" + min + ", " + max + "]: " + JSON.toJSONString(albumIds));
            min = max;
            for (Integer albumId : albumIds) {
                materielService.getAlbumInfoBO(albumId);
            }
        }
        long end = System.currentTimeMillis();
        logger.info("cacheAlbum total time: " + (end - begin) / 1000.0);

    }

    @Override
    public void cacheArtistInfo(int maxArtistId) {
        int maxId = artistDAO.selectMaxArtistId();
        int totalNum = maxArtistId > maxId ? maxId : maxArtistId;
        int totalIdsPerPage = 1000;
        int totalPage = totalNum / totalIdsPerPage + 1;

        int min = 0;
        int max = 0;
        long begin = System.currentTimeMillis();

        for (int i = 0; i < totalPage; i++) {
            max = (i + 1) * totalIdsPerPage;
            max = max > totalNum ? totalNum : max;
            List<Integer> artistIds = artistDAO.selectArtistIds(min, max);
            logger.info("artistId[" + min + ", " + max + "]: " + JSON.toJSONString(artistIds));
            min = max;
            for (Integer artistId : artistIds) {
                materielService.getArtistInfoBO(artistId);
            }
        }
        long end = System.currentTimeMillis();
        logger.info("cacheArtist total time: " + (end - begin) / 1000.0);

    }

    @Override
    public AlbumInfoBO getAlbumInfoBO(int n, int albumId, int cnt) {
        if (albumId == 0) {
            return null;
        }
        AlbumInfoBO albumInfoBO = materielService.getAlbumInfoBO(albumId);
        if (albumInfoBO == null) {
            return null;
        }
        List<RelatedPartyStatus> relItems = albumInfoBO.getRelItems();
        if (CollectionUtils.isEmpty(relItems)) {
            return albumInfoBO;
        }
        n = (n <= 0 ? 1 : n);
        for (int i = 0; i < n; i++) {
            List<Integer> itemIds = new ArrayList<>();
            for (RelatedPartyStatus status : relItems) {
                itemIds.add(status.getId());
            }
            /*int index = 0;
            int count = itemIds.size() / 20;
            for (int j = 0; j < count; j++) {
                index = (j+1)*20;
                materielService.getItemInfoBOList(itemIds.subList(j*20, (j+1)*20), cnt);
            }
            if (index < itemIds.size()) {
                materielService.getItemInfoBOList(itemIds.subList(index, itemIds.size()), cnt);
            }*/
            materielService.getItemInfoBOList(itemIds, cnt);
        }

        return albumInfoBO;
    }

    @Override
    public AlbumInfoBO getAlbumInfoBOFromRedis(int count, int albumId) {
        if (albumId == 0) {
            return null;
        }
        AlbumInfoBO albumInfoBO = materielService.getAlbumInfoBOFromRedis(albumId);
        if (albumInfoBO == null) {
            return null;
        }
        List<RelatedPartyStatus> relItems = albumInfoBO.getRelItems();
        if (CollectionUtils.isEmpty(relItems)) {
            return albumInfoBO;
        }
        count = (count <= 0 ? 1 : count);
        for (int i = 0; i < count; i++) {
            List<Integer> itemIds = new ArrayList<>();
            for (RelatedPartyStatus status : relItems) {
                itemIds.add(status.getId());
            }
            materielService.getItemInfoBOListFromRedis(itemIds);
        }
        return albumInfoBO;
    }

    @Override
    public ItemInfoBO getItemInfoBO(int itemId) {
        ItemInfoBO result = materielService.getItemInfoBO(itemId);
        return result;
    }

}
