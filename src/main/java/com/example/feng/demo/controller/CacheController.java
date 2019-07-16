package com.example.feng.demo.controller;


import com.example.feng.demo.service.IMaterielCacheService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "cache")
public class CacheController {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private IMaterielCacheService materielCacheService;

    @RequestMapping("/cacheItemInfo")
    public String cacheItemInfo(int maxId) {
        logger.info("cacheItemInfo begin: " + maxId);
        final String traceId = MDC.get("traceId");
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                MDC.put("traceId", traceId);
                materielCacheService.cacheItemInfo(maxId);
            }
        });
        t.start();

        return "cacheItemInfo success";
    }

    @RequestMapping("/cacheAlbumInfo")
    public String cacheAlbumInfo(int maxId) {
        logger.info("cacheAlbumInfo begin: " + maxId);
        final String traceId = MDC.get("traceId");
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                MDC.put("traceId", traceId);
                materielCacheService.cacheAlbumInfo(maxId);
            }
        });
        t.start();

        return "cacheAlbumInfo success";
    }

    @RequestMapping("/cacheArtistInfo")
    public String cacheArtistInfo(int maxId) {
        logger.info("cacheArtistInfo begin: " + maxId);
        final String traceId = MDC.get("traceId");
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                MDC.put("traceId", traceId);
                materielCacheService.cacheArtistInfo(maxId);
            }
        });
        t.start();

        return "cacheArtistInfo success";
    }
}
