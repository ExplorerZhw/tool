package com.util;

import java.io.FileWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhw on 2018/1/30.
 */
public class SqlUtil {
    String namespace = "(?:`|\")(GTMIS|gtmis|GTMIS_GZ|gtmis_gz)(?:`|\")(?:\\.)";

    //mysqldump -h localhost -uroot -p123456 gtmis_gz jcw_group_post > d:\dump.sql
    public String toOracle(String sql) {
        if (CodeUtil.isNotEmpty(sql)) {
            sql = sql.trim();
            sql = toMysql(sql);
//            if (sql.contains("\"")) {
//            }
//            if (temp.startsWith("insert") || temp.startsWith("update") || temp.startsWith("delete")) {
            sql = replaceDateTimeToOracle(sql, "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}", "TO_DATE(???, 'SYYYY-MM-DD HH24:MI:SS')");
            sql = replaceDateTimeToOracle(sql, "\\d{4}-\\d{2}-\\d{2}", "TO_DATE(???, 'SYYYY-MM-DD')");
//                String[] split = searchByRegex(sql, "`.*?`\\.`.*?`").split("\\.");
//                String namespace = split.length > 0 ? split[0] + "." : "";
            sql = sql.replaceAll(namespace, "").replaceAll("`", "");
//                sql = SqlFormat.format(sql);
//            }
        }
        return sql;
    }

    public String toMysql(String sql) {
        String result = "";
        if (CodeUtil.isNotEmpty(sql)) {
            sql = sql.trim();
            String[] splitArr = sql.split("\n");
            for (String oneSql : splitArr) {
                String temp = oneSql.toLowerCase();
                oneSql = oneSql.replaceAll("`", "\"");
//            String[] split = searchByRegex(oneSql, "\".*?\"\\.\".*?\"").split("\\.");
//            String namespace = split.length > 0 ? split[0] + "." : "";
                oneSql = replaceDateTimeToMysql(oneSql).replaceAll(namespace, "");
                if (temp.startsWith("insert")) {
                    if (temp.contains("rowid")) {
                        String rowValue = searchByRegex(oneSql, ",\\s+'[^']+'");
                        String rowId = searchByRegex(oneSql, ",( )+\"(ROWID|rowid)\"");
                        oneSql = oneSql.replace(rowId, "").replace(rowValue, "");
                    }
                    oneSql = oneSql.replaceAll("\"", "");
//                oneSql = oneSqlFormat.format(oneSql);
                } else if (temp.startsWith("update")) {
                    if (temp.contains("rowid")) {
                        String pk = searchByRegex(oneSql, "(SET|set)\\s*?\".*?\"='.*?'").replaceAll("SET|set", "");
                        oneSql = replaceRowId(oneSql).replaceAll("WHERE|where", "WHERE " + pk);
                    }
                    oneSql = oneSql.replaceAll("\"", "");
//                oneSql = oneSqlFormat.format(oneSql);
                }
                result += "\n" + oneSql;
            }

        }
        return result;
    }


    // 根据正则匹配内容
    public String searchByRegex(String source, String reg) {
        Pattern p = Pattern.compile(reg);
        Matcher m = p.matcher(source);
        String result = "";
        if (m.find()) {
            result = m.group(0);
        }
        return result;
    }

    // 根据正则匹配内容
//    public static String searchByRegexEnd(String source, String reg) {
//        Pattern p = Pattern.compile(reg);
//        Matcher m = p.matcher(source);
//        String result = "";
//        while (m.find()) {
//            result = m.group(0);
//        }
//        return result;
//    }

    public String replaceRowId(String source) {
        source = source.replace(searchByRegex(source, ",*?\\s*?\"??ROWID\"??\\s*=\\s*'.*?'"), "");
        source = source.replace(searchByRegex(source, ",*?\\s*?\"??ROWID\"??\\s*=\\s*'.*?'"), "");
        return source;
    }

    public String replaceDateTimeToMysql(String source) {
        while (source.contains("TO_DATE")) {
            String toDate = searchByRegex(source, "TO_DATE\\(.*?\\)");
            if (CodeUtil.isEmpty(toDate)) break;
            String[] split = toDate
                    .replace("TO_DATE(", "")
                    .replace(")", "")
                    .split("\\,");
            String date = split.length > 0 ? split[0] : "";
            source = source.replace(toDate, date);
        }
        return source;
    }

    public String replaceDateTimeToOracle(String source, String format, String toReplace) {
        format = "'" + format + "'";
        String time = searchByRegex(source, format);
        java.util.List<String> replaceList = new ArrayList<>();
        while (!"".equals(time)) {
            replaceList.add(toReplace.replace("???", time));
            source = source.replace(time, "###");
            time = searchByRegex(source, format);
        }
        for (String str : replaceList) {
            source = source.replace("###", str);
        }
        return source;
    }

    // 导出Oracle表结构
    private static final String TYPE_MARK = "-1";
    private static String SQL =
            "SELECT DBMS_METADATA.GET_DDL(U.OBJECT_TYPE, U.object_name), U.OBJECT_TYPE " +
                    "FROM USER_OBJECTS U " +
                    "where U.OBJECT_TYPE = 'TABLE' " +
                    "or U.OBJECT_TYPE = 'VIEW' " +
                    "or U.OBJECT_TYPE = 'INDEX' " +
                    "or U.OBJECT_TYPE = 'PROCEDURE' " +
                    "or U.OBJECT_TYPE = 'SEQUENCE' " +
                    "or U.OBJECT_TYPE = 'TRIGGER' " +
                    "order by U.OBJECT_TYPE desc";

    private static String URL = "jdbc:oracle:thin:@127.0.0.1:1521:tmis";
    private static String USERNAME = "gtmis_hy";
    private static String PASSWORD = "gtmis_hy";
    private static String OUTFILE = "tables.sql";

    public void exportOracelTable() throws Exception {
        FileWriter fw = new FileWriter(OUTFILE);
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection con = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery(SQL);
        Clob ddl;
        String type = TYPE_MARK;
        int count = 0;
        List<String> list = new ArrayList<>();
        while (rs.next()) {
            ddl = rs.getClob(1);
            fw.write(ddl.getSubString(1L, (int) ddl.length()));
            if (!rs.getString(2).equals(type)) {
                if (!type.equals(TYPE_MARK)) {
                    list.add(type + "," + count);
                    type = rs.getString(2);
                    count = 1;
                } else {
                    type = rs.getString(2);
                    count++;
                }
            } else
                count++;
        }
        list.add(type + "," + count);
        fw.flush();
        fw.close();
        rs.close();
        statement.close();
        con.close();
        for (String type1 : list)
            System.out.print(type1.split(",")[0] + ":" + type1.split(",")[1] + ";");
        System.out.println();
    }

    public static void main(String[] args) throws Exception {

    }
}
