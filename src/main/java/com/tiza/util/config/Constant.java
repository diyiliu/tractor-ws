package com.tiza.util.config;

import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description: Constant
 * Author: DIYILIU
 * Update: 2016-03-24 15:58
 */
public final class Constant {

    public enum Protocol {
        ;
        public final static int M2_REPEAT_COUNT = 2;
        public final static int M2_REPEAT_TIME = 8;
        public final static int MOBILE_REPEAT_COUNT = 2;
        public final static int MOBILE_REPEAT_TIME = 10;
    }


    public enum DBInfo {
        ;
        public final static String DB_OP_INSERT = "INSERT";
        public final static String DB_OP_UPDATE = "UPDATE";

        public final static String DB_TRACTOR_USER = "TRACTOR";
        public final static String DB_TRACTOR_INSTRUCTION = "LOG_GPSINSTRUCTION";
        public final static String DB_TRACTOR_RAWDATA = "gpsrawdata";
        public final static String DB_TRACTOR_VEHICLEGPSINFO = "vehiclegpsinfo";
        public final static String DB_TRACTOR_VEHICLETRACK = "vehicletrack";
        public final static String DB_TRACTOR_VEHICLEWORKPARAM= "vehicleworkparam";

    }

    public void init() {
//        initSqlCache();

    }

    private final static String SQL_FILE = "sql.xml";

    private static Map<String, String> sqlCache = new HashMap<>();

    public static String getSQL(String sqlId) {
        return sqlCache.get(sqlId);
    }

    public void initSqlCache() {
        sqlCache.clear();

        InputStream is = null;
        try {
            is = new ClassPathResource(SQL_FILE).getInputStream();
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(is);

            List<Node> sqlList = document.selectNodes("root/sql");
            for (Node sqlNode : sqlList) {
                String id = sqlNode.valueOf("@id");
                String content = sqlNode.getText().trim();
                sqlCache.put(id, content);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
