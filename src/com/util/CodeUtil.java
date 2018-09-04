package com.util;

import com.bean.TableModel;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.List;

/**
 * Created by ahoi on 2017/12/6 0006.
 * 生成MVC的各层JAVA文件
 */
public class CodeUtil {

    private String packageName = "";
    private String tableName = "";
    private String author = "xxx";
    // 注释类型，0-普通注释，1-swagger2注释
    private Integer annotationType = 0;
    private static final Map<String, String> comments = new HashMap<>();
    public static final List<String> allTableNames = new ArrayList<>();

    private static String driverClass = "oracle.jdbc.driver.OracleDriver";
    //        private static final String url = "jdbc:oracle:thin:@192.168.1.161:1521:tmis";
    private static String url = "jdbc:oracle:thin:@www.cdtye.com:1111:tmis";
    private static String username = "gtmis";
    private static String password = "gtmis";
    private static final String newLine = "\r\n";
    private static final String sql_query_column = "select a.column_name,a.data_type,a.data_length,a.data_scale,b.comments from user_tab_columns a, user_col_comments b where a.table_name = b.table_name and a.column_name = b.column_name and a.table_name = upper('{tableName}') order by a.column_id";
    private static final String sql_query_table = "select distinct a.table_name, b.comments from user_tab_columns a, user_tab_comments b where a.table_name = b.table_name order by a.table_name";
    //    private static final String filePath = "F:\\project\\TMIS\\trunk\\Code\\zoomtech-gzims-jcw\\src\\main\\java\\{packageName}\\{className}.java";
    private String filePath = "D:\\";
    private static String fileName = "{className}.java";
    private static String className = "";
    List<TableModel> tableModels = new ArrayList<>();

    private static Connection conn = null;
    private static PreparedStatement pst = null;

    FileUtil fileUtil = new FileUtil();

    // 设置表名
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    // 设置作者
    public void setAuthor(String author) {
        this.author = author;
    }

    // 设置注释类型
    public void setAnnotationType(int annotationType) {
        this.annotationType = annotationType;
    }

    // 设置包名
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    // 设置生成文件路径
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public CodeUtil(String driverClass, String url, String username, String password) {
        this.driverClass = driverClass;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    // 连接数据库
    public Integer connect() {
        int temp = 0;
        try {
            conn = null;
            Class.forName(driverClass);
            conn = DriverManager.getConnection(url, username, password);
            temp = 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return temp;
    }

    // 关闭数据库连接
    public void close() {
        try {
            if (isConnect()) {
                conn.close();
                pst.close();
            }
            conn = null;
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "数据库连接错误: \r\n" + e.toString(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 初始化数据连接
    public void init() {
        try {
            pst = conn.prepareStatement(sql_query_column.replace("{tableName}", tableName));
            ResultSet ret = pst.executeQuery();
            className = toJavaClassName(tableName);
            tableModels.clear();

            while (ret.next()) {
                // 获取数据库表的各项信息
                String colName = ret.getString(1);// 字段名
                String colType = ret.getString(2);// 字段类型
                String colLength = ret.getString(3);// 字段长度
                String colScale = ret.getString(4);// 字段小数点
                String colComments = ret.getString(5);// 字段注释
//                System.out.println(colName + "\t" + colType + "\t" + colLength + "\t" + colScale + "\t" + colComments);
                TableModel table = new TableModel(colName, colType, colLength, colScale, colComments);
                tableModels.add(table);
            }
            ret.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 查询数据库的所有表，并保存表注释
    public List<String> queryTable() {
        List<String> tables = new ArrayList<>();
        try {
            pst = conn.prepareStatement(sql_query_table);
            ResultSet ret = pst.executeQuery();
            while (ret.next()) {
                String tableName = ret.getString(1);
                String tableComments = ret.getString(2);
                tables.add(tableName);
                allTableNames.add(tableName);
                comments.put(tableName, tableComments);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tables;
    }

    // 生成controller文件
    public void generateController() {
        try {
            className = toJavaClassName(tableName);
            Util util = new Util();
            // 生成头部信息
            String content = "";
            if (isNotEmpty(packageName)) content += "package " + packageName + ".controller;" + newLine;
            content += newLine;
            content += "import javacommon.base.BaseSpringController;" + newLine;
            content += "import javacommon.base.ResponseJson;" + newLine;
            content += "import org.springframework.stereotype.Controller;" + newLine;
            content += "import org.springframework.web.bind.annotation.RequestMapping;" + newLine;
            content += "import javax.servlet.http.HttpServletRequest;" + newLine;
            content += "import javax.servlet.http.HttpServletResponse;" + newLine;
            content += newLine;
            content += "/**" + newLine;
            content += " * Created by " + author + " on " + util.formatDate(new Date(), util.FORMAT_FULL) + "." + newLine;
            content += " * " + comments.get(tableName) + newLine;
            content += " */" + newLine;
            if (annotationType == 0) {
                content += "@Controller" + newLine;
                content += "@RequestMapping(value = \"/pages/" + className + "\")" + newLine;
                content += "public class " + className + "Controller extends BaseSpringController {" + newLine;
                content += newLine;
                content += "    /**" + newLine;
                content += "     * you must write desc here..." + newLine;
                content += "     */" + newLine;
            } else if (annotationType == 1) {
                content += "@Controller" + newLine;
                content += "@RequestMapping(value = \"/pages/" + className + "\")" + newLine;
                content += "@Api(tags = \"XXX\")";
                content += newLine;
                content += "public class " + className + "Controller extends BaseSpringController {" + newLine;
                content += newLine;
                content += "    @RequestMapping(value = \"/function.do\")" + newLine;
                content += "    @ApiOperation(value = \"方法说明使用\", httpMethod = \"GET\", response = ResponseJson.class)" + newLine;
            }
            content += "    public void test(HttpServletRequest request, HttpServletResponse response) {" + newLine;
            content += "        try {" + newLine;
            content += "            ResponseJson.writeSuccess(response);" + newLine;
            content += "        } catch (Exception e) {" + newLine;
            content += "            e.printStackTrace();" + newLine;
            content += "            ResponseJson.writeError(response);" + newLine;
            content += "        }" + newLine;
            content += "    }" + newLine;
            content += newLine;
            content += "}" + newLine;
            // 生成文件
            fileUtil.createFile(content, className + "Controller", packageName, filePath, fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 生成manager文件
    public void generateManager() {
        try {
            Util util = new Util();
            className = toJavaClassName(tableName);
            String fieldName = toJavaFieldName(tableName);
            // 生成头部信息
            String content = "";
            if (isNotEmpty(packageName)) content += "package " + packageName + ".manager;" + newLine;
            content += newLine;
            if (isNotEmpty(packageName)) {
                content += "import " + packageName + "." + className + "Dao;" + newLine;
                content += "import " + packageName + "." + className + ";" + newLine;
            }
            content += "import javacommon.base.BaseManager;" + newLine;
            content += "import javacommon.base.EntityDao;" + newLine;
            content += "import org.springframework.beans.factory.annotation.Autowired;" + newLine;
            content += "import org.springframework.stereotype.Service;" + newLine;
            content += newLine;
            content += "/**" + newLine;
            content += " * Created by " + author + " on " + util.formatDate(new Date(), util.FORMAT_FULL) + "." + newLine;
            content += " * " + comments.get(tableName) + newLine;
            content += " */" + newLine;
            content += "@Service" + newLine;
            content += "public class " + className + "Manager extends BaseManager<" + className + ", String> {" + newLine;
            content += newLine;
            content += "    @Autowired" + newLine;
            content += "    private " + className + "Dao " + fieldName + "Dao;" + newLine;
            content += newLine;
            content += "    @Override" + newLine;
            content += "    protected EntityDao getEntityDao() {" + newLine;
            content += "        return " + fieldName + "Dao;" + newLine;
            content += "    }" + newLine;
            content += newLine;
            content += "    /**" + newLine;
            content += "     * 查询数据" + newLine;
            content += "     */" + newLine;
            content += "    public List<" + className + "> listData(" + className + " query) {" + newLine;
            content += "        return " + fieldName + "Dao.listData(query);" + newLine;
            content += "    }" + newLine;
            content += newLine;
            content += "}" + newLine;
            // 生成文件
            fileUtil.createFile(content, className + "Manager", packageName, filePath, fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 生成dao文件
    public void generateDao() {
        try {
            Util util = new Util();
            className = toJavaClassName(tableName);
            // 生成头部信息
            String content = "";
            if (isNotEmpty(packageName)) content += "package " + packageName + ".dao;" + newLine;
            content += newLine;
            if (isNotEmpty(packageName)) content += "import " + packageName + "." + className + ";" + newLine;
            content += "import javacommon.base.BaseHibernateDao;" + newLine;
            content += "import org.springframework.stereotype.Repository;" + newLine;
            content += "import javacommon.codeUtil.RequestUtil;" + newLine;
            content += "import java.codeUtil.List;" + newLine;
            content += newLine;
            content += "/**" + newLine;
            content += " * Created by " + author + " on " + util.formatDate(new Date(), util.FORMAT_FULL) + "." + newLine;
            content += " * " + comments.get(tableName) + newLine;
            content += " */" + newLine;
            content += "@Repository" + newLine;
            content += "public class " + className + "Dao extends BaseHibernateDao<" + className + ", String> {" + newLine;
            content += newLine;
            content += "    @Override" + newLine;
            content += "    public Class getEntityClass() {" + newLine;
            content += "        return " + className + ".class;" + newLine;
            content += "    }" + newLine;
            content += newLine;
            content += "    /**" + newLine;
            content += "     * 查询数据" + newLine;
            content += "     */" + newLine;

            // listData方法拼接new map字段
            content += "    public List<" + className + "> listData(" + className + " query) {" + newLine;
            content += "        String hql = \"select new map( \" + " + newLine;
            for (int i = 0; i < tableModels.size(); i++) {
                TableModel table = tableModels.get(i);
                content += "                \" t." + table.getJavaFieldName() + " as " + table.getJavaFieldName();
                if (i < tableModels.size() - 1) content += ", ";
                content += "\" + " + newLine;
            }
            content += "               \" ) from " + className + " t\";" + newLine;
            content += "        List list = findAllByHqlAndValueBean(hql.toString(), query);" + newLine;
            content += "        return RequestUtil.parseListToList(list, " + className + ".class);" + newLine;
            content += "    }" + newLine;

            content += newLine;
            content += "}" + newLine;

            // 生成文件
            fileUtil.createFile(content, className + "Dao", packageName, filePath, fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 生成model文件
    public void generateModel() {
        try {
            String filedContent = "";
            String methodContent = "";
            String javaType = "";
            Boolean primaryKey = true;
            for (TableModel table : tableModels) {
                // 获取数据库表的各项信息
                String colName = table.getColName();// 字段名
                String colType = table.getColType();// 字段类型
                String colLength = table.getColLength();// 字段长度
                String colScale = table.getColScale();// 字段小数点
                String colComments = table.getColComments();// 字段注释
                String javaFieldName = table.getJavaFieldName();
                String javaClassName = table.getJavaClassName();
                String javaPrecision = "";

                // 将数据库表字段类型转化为java类型，默认为String
                if ("DATE".equals(colType)) {
                    javaType = "Date";
                } else if ("NUMBER".equals(colType)) {
                    if (isEmpty(colScale) || "0".equals(colScale)) {
                        javaType = "Integer";
                    } else {
                        javaType = "Double";
                        javaPrecision = ", precision = " + colScale;
                    }
                } else if ("FLOAT".equals(colType)) {
                    javaType = "Double";
                } else if ("BLOB".equals(colType)) {
                    javaType = "Blob";
                } else {
                    javaType = "String";
                }

                // 生成成员变量和getset方法
                filedContent += "    // " + colComments + "  -->  COLUMN:  " + colName + newLine;
                filedContent += "    private " + javaType + " " + javaFieldName + ";" + newLine;
                if ("Date".equals(javaType)) {
                    filedContent += "    private String " + javaFieldName + "String;" + newLine;
                }

                // 主键需要添加uuid主键注解
                if (primaryKey) {
                    methodContent += "    @Id" + newLine;
                    methodContent += "    @GeneratedValue(generator = \"uuid\")" + newLine;
                    methodContent += "    @GenericGenerator(name = \"uuid\", strategy = \"uuid\")" + newLine;
                    primaryKey = false;
                }
                methodContent += "    @Column(name = \"" + colName + "\", nullable = true, length = " + colLength + javaPrecision + ")" + newLine;
                methodContent += "    public " + javaType + " get" + javaClassName + "() {" + newLine;
                methodContent += "        return " + javaFieldName + ";" + newLine;
                methodContent += "    }" + newLine;
                methodContent += newLine;
                methodContent += "    public void set" + javaClassName + "(" + javaType + " " + javaFieldName + ") {" + newLine;
                methodContent += "        this." + javaFieldName + " = " + javaFieldName + ";" + newLine;
                // 日期类型特殊处理，添加transient字段接收其对应的String类型
                if ("Date".equals(javaType)) {
                    methodContent += "        if (" + javaFieldName + " != null) {" + newLine;
                    methodContent += "            this." + javaFieldName + "String = DateUtils.formatDate(" + javaFieldName + ", DateUtils.FORMAT_FULL);" + newLine;
                    methodContent += "        }" + newLine;
                }
                methodContent += "    }" + newLine;
                methodContent += newLine;

                if ("Date".equals(javaType)) {
                    methodContent += "    @Transient" + newLine;
                    methodContent += "    public String get" + javaClassName + "String() {" + newLine;
                    methodContent += "        return " + javaFieldName + "String;" + newLine;
                    methodContent += "    }" + newLine;
                    methodContent += newLine;
                    methodContent += "    public void set" + javaClassName + "String(String " + javaFieldName + "String) {" + newLine;
                    methodContent += "        this." + javaFieldName + "String = " + javaFieldName + "String;" + newLine;
                    methodContent += "        if (StringUtils.isNotEmpty(" + javaFieldName + "String)) {" + newLine;
                    methodContent += "            this." + javaFieldName + " = DateUtils.createDate(" + javaFieldName + "String);" + newLine;
                    methodContent += "        }" + newLine;
                    methodContent += "    }" + newLine;
                    methodContent += newLine;
                }
            }
            String content = getModelBaseContent(tableName) + filedContent + newLine + newLine + methodContent + "}" + newLine;

            // 生成文件
            fileUtil.createFile(content, className, packageName, filePath, fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 生成model文件的头部信息
    public String getModelBaseContent(String tableName) {
        String content = "";
        Util util = new Util();
        if (isNotEmpty(packageName)) content += "package " + packageName + ".model;" + newLine;
        content += newLine;
        content += "import javacommon.codeUtil.DateUtils;" + newLine;
        content += "import org.apache.commons.lang.StringUtils;" + newLine;
        content += "import org.hibernate.annotations.GenericGenerator;" + newLine;
        content += "import javax.persistence.*;" + newLine;
        content += "import java.io.Serializable;" + newLine;
        content += "import java.codeUtil.Date;" + newLine;
        content += newLine;
        content += "/**" + newLine;
        content += " * Created by " + author + " on " + util.formatDate(new Date(), util.FORMAT_FULL) + "." + newLine;
        content += " * " + comments.get(tableName) + newLine;
        content += " */" + newLine;
        content += "@Entity" + newLine;
        content += "@Table(name = \"" + tableName + "\")" + newLine;
        content += "public class " + className + " implements Serializable {" + newLine;
        content += newLine;
        return content;
    }

    // 转换为驼峰命名【首字母全部大写】
    public static String toJavaClassName(String colName) {
        String result = "";
        String[] split = colName.split("_");
        for (String str : split) {
            if (isEmpty(str)) continue;
            result += str.substring(0, 1).toUpperCase() + str.substring(1, str.length()).toLowerCase();
        }
        return result;
    }

    // 转换为驼峰命名【第一个首字母小写，其他首字母全部大写】
    public static String toJavaFieldName(String colName) {
        String result = toJavaClassName(colName);
        result = result.substring(0, 1).toLowerCase() + result.substring(1, result.length());
        return result;
    }

    // 判断当前数据库是否保持连接
    public Boolean isConnect() {
        return conn != null;
    }

    public static boolean isNotEmpty(Object o) {
        return o != null && !"".equals(o.toString()) && !"null".equals(o.toString());
    }

    public static boolean isEmpty(Object o) {
        return !isNotEmpty(o);
    }


}
