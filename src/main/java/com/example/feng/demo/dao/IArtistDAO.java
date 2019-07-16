package com.example.feng.demo.dao;

import java.util.List;

public interface IArtistDAO {

    int selectMaxArtistId();

    List<Integer> selectArtistIds(int min, int max);
}
