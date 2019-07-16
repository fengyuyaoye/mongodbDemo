package com.example.feng.demo.executor;

import com.atzy.materiel.service.model.AlbumInfoBO;
import com.example.feng.demo.service.IMaterielCacheService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Kevin Xiao
 * @version 1.0
 * @Description TODO
 * @since 2019/7/1
 */
@Component
public class CacheAccessorExecutor {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private IMaterielCacheService materielCacheService;

    public void accessAlbum(int nThreads, int n, int albumId, int total) {
        long begin = System.currentTimeMillis();
        AlbumInfoBO albumInfoBO = materielCacheService.getAlbumInfoBO(n, albumId, total);
        long end = System.currentTimeMillis();
        logger.info("nThreads[{}], album[id={}], total item is {}, total time: {}", nThreads, albumInfoBO.getAlbumId(), albumInfoBO.getRelItems().size(), (end - begin));
    }

    public void accessAlbumFromRedis(int nThreads, int n, int albumId) {
        long begin = System.currentTimeMillis();
        AlbumInfoBO albumInfoBO = materielCacheService.getAlbumInfoBOFromRedis(n, albumId);
        long end = System.currentTimeMillis();
        if (albumInfoBO != null) {
            logger.info("nThreads[{}], album[id={}], total item is {}, total time: {}",
                    nThreads, albumInfoBO.getAlbumId(), albumInfoBO.getRelItems().size(), (end - begin));
        } else {
            logger.info("albumInfoBO[id={}] in null.", albumId);
        }

    }
}
