package com.tiza.util.thread;

import com.tiza.util.JacksonUtil;
import com.tiza.util.cache.ICache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;

/**
 * Description: SqlProcess
 * Author: DIYILIU
 * Update: 2017-06-13 10:04
 */
public class SqlProcess implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ICache sqlPool;
    private int maxSize;
    private String type;
    private String processName;

    private JdbcTemplate jdbcTemplate;

    public SqlProcess(ICache sqlPool, int maxSize, String type) {

        this.sqlPool = sqlPool;
        this.maxSize = maxSize;
        this.type = type;
    }

    public SqlProcess(ICache sqlPool, int maxSize, String type, String processName, JdbcTemplate jdbcTemplate) {

        this.sqlPool = sqlPool;
        this.maxSize = maxSize;
        this.type = type;
        this.processName = processName;
        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    public void run() {
        //logger.debug("数据库操作线程[{}]执行中...", processName);

        if (sqlPool.size() < 1){

            return;
        }
        Set<Object> keys = sqlPool.getKeys();
        List<String> sqlList = new ArrayList(maxSize);

        int i = 0;
        for (Iterator iterator = keys.iterator(); iterator.hasNext();){
            String key = (String) iterator.next();
            String value = (String) sqlPool.get(key);

            sqlPool.remove(key);
            sqlList.add(value);
            if (++i > maxSize){

                batch(sqlList, type);
                sqlList.clear();
            }

        }

        if (sqlList.size() > 0){

            batch(sqlList, type);
            sqlList.clear();
        }

    }

    public void batch(List<String> sqlList, String type) {
        String[] sqlArray = sqlList.toArray(new String[sqlList.size()]);
        Date t1 = new Date();
        Date t2;
        try {
            jdbcTemplate.batchUpdate(sqlArray);
            t2 = new Date();
            logger.debug("批处理： 类型[{}]，数量[{}]，耗时[{}毫秒], SQL{}", type, sqlArray.length, (t2.getTime() - t1.getTime()), JacksonUtil.toJson(sqlArray));
        } catch (BadSqlGrammarException e) {
            t2 = new Date();
            logger.error("SQL错误！类型[{}]，耗时[{}毫秒], SQL[{}]， 描述[{}]", type, (t2.getTime() - t1.getTime()), e.getSql(), e.getSQLException().getMessage());
        } catch (DataAccessException e) {
            execute(sqlList);
            t2 = new Date();
            logger.warn("异常中断： 类型[{}]，数量[{}]，耗时[{}毫秒]", type, sqlArray.length, (t2.getTime() - t1.getTime()));
        }
    }

    public void execute(List<String> sqlList) {
        for (String sql : sqlList) {
            try {
                jdbcTemplate.execute(sql);
            } catch (DataAccessException e) {
                logger.error("SQL错误！[{}], {}", sql, e.getMessage());
            }
        }
    }
}
