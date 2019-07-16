package com.example.feng.demo.dao;

import java.util.List;

public interface IContentFileDAO {

    int selectMaxContentFileId();

    List<Integer> selectContentFileIds(int min, int max);
}
