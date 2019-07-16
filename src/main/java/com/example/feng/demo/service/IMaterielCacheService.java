package com.example.feng.demo.service;

import com.atzy.materiel.service.model.AlbumInfoBO;
import com.atzy.materiel.service.model.ItemInfoBO;

public interface IMaterielCacheService {

    void cacheItemInfo(int maxItemId);

    void cacheAlbumInfo(int maxAlbumId);

    void cacheArtistInfo(int maxArtistId);

    AlbumInfoBO getAlbumInfoBO(int n, int albumId, int cnt);

    AlbumInfoBO getAlbumInfoBOFromRedis(int count, int albumId);

    ItemInfoBO getItemInfoBO(int itemId);
}
