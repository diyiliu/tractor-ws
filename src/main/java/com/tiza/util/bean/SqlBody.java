package com.tiza.util.bean;

/**
 * Description: SqlBody
 * Author: DIYILIU
 * Update: 2017-06-12 17:03
 */
public class SqlBody {

    // sql类型（insert/update）
    private String type;
    // sql关键字（主键）
    private String key;
    private String table;
    private String sql;


    public SqlBody() {
    }

    public SqlBody(String type, String table, String sql, String key) {
        this.type = type;
        this.key = key;
        this.table = table;
        this.sql = sql;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }
}
