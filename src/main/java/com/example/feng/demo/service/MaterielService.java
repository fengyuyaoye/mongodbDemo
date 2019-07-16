package com.example.feng.demo.service;

import com.atzy.materiel.service.model.AlbumInfoBO;
import com.atzy.materiel.service.model.ArtistInfoBO;
import com.atzy.materiel.service.model.ItemInfoBO;

import java.util.List;

/**
 * @author Kevin Xiao
 * @version 1.0
 * @Description TODO
 * @since 2019/7/1
 */
public interface MaterielService {
    ItemInfoBO getItemInfoBO(int id);

    List<ItemInfoBO> getItemInfoBOList(List<Integer> ids, int cnt);

    List<ItemInfoBO> getItemInfoBOListFromRedis(List<Integer> ids);

    AlbumInfoBO getAlbumInfoBO(int id);

    AlbumInfoBO getAlbumInfoBOFromRedis(int id);

    ArtistInfoBO getArtistInfoBO(int id);
}
