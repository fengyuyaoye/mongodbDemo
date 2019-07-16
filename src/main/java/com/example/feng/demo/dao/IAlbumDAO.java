package com.example.feng.demo.dao;

import java.util.List;

public interface IAlbumDAO {

    int selectMaxAlbumId();

    List<Integer> selectAlbumIds(int min, int max);
}
