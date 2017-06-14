package com.tiza.util.client.impl;

import com.tiza.util.bean.SqlBody;
import com.tiza.util.cache.ICache;
import com.tiza.util.client.IClient;
import com.tiza.util.config.Constant;
import com.tiza.util.thread.SqlProcess;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Resource;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Description: DBClient
 * Author: DIYILIU
 * Update: 2017-06-12 14:24
 */
public class DBClient implements IClient {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private ICache insertSqlCacheProvider;

    @Resource
    private ICache updateSqlCacheProvider;

    private ScheduledExecutorService insertExecutorService = Executors.newScheduledThreadPool(2);

    private ScheduledExecutorService updateExecutorService = Executors.newScheduledThreadPool(1);

    @Override
    public void init() {

        insertExecutorService.scheduleAtFixedRate(new SqlProcess(insertSqlCacheProvider,
                10, Constant.DBInfo.DB_OP_INSERT, "insert-1", jdbcTemplate), 1, 2, TimeUnit.SECONDS);

        insertExecutorService.scheduleAtFixedRate(new SqlProcess(insertSqlCacheProvider,
                10, Constant.DBInfo.DB_OP_INSERT, "insert-2", jdbcTemplate), 2, 2, TimeUnit.SECONDS);

        updateExecutorService.scheduleAtFixedRate(new SqlProcess(updateSqlCacheProvider,
                10, Constant.DBInfo.DB_OP_UPDATE, "update-1", jdbcTemplate), 1, 1, TimeUnit.SECONDS);

    }
}
