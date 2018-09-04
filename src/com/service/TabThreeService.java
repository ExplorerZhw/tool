package com.service;

import com.util.SqlUtil;

public class TabThreeService {
    public TabThreeService() {
    }

    public SqlUtil sqlUtil = new SqlUtil();

    public String toOracle(String mysql) {
        return sqlUtil.toOracle(mysql);
    }

    public String toMysql(String oracle) {
        return sqlUtil.toMysql(oracle);
    }

    public String toMysqlCreate(String cmds, String spl) {
        String mysqlOut = "";
        String mysqlIn = "";
        String oracleOut = "";
        return "";
    }
}
