package com.example.feng.demo.dao;

import java.util.List;

public interface IItemDAO {

    int selectMaxItemId();

    List<Integer> selectItemIds(int min, int max);
}
