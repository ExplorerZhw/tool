package com.util;

import java.io.*;
import java.util.Properties;

public class ConfigUtil {
    // 配置配置
    Properties prop = null;
    public String driver = "oracle.jdbc.driver.OracleDriver";
    public String url_mysql = "jdbc:mysql://localhost:3306/gtmis";
    public String url_oracle = "jdbc:oracle:thin:@192.168.1.232:1521:tmis";
    public String username = "gtmis";
    public String password = "gtmis";
    public String path = "D:\\";
    public String author = "";
    String configPath = new File("").getAbsolutePath() + "/config.properties";
    File configFile = new File(configPath);
    // 加载状态 0-未加载；1-加载
    private int fileLoadState = 0;

    public int getFileLoadState() {
        return fileLoadState;
    }

    public void setFileLoadState(int fileLoadState) {
        this.fileLoadState = fileLoadState;
    }

    // 初始化配置
    public void initConfig() {
        System.out.println("configPath-" + configPath);
        try {
            if (configFile.exists()) {
                InputStream is = new FileInputStream(configFile);
                if (is != null) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    prop = new Properties();
                    prop.load(br);
                    if (CodeUtil.isNotEmpty(prop.get("driver"))) {
                        driver = prop.get("driver").toString();
                    }
                    if (CodeUtil.isNotEmpty(prop.get("url_mysql"))) {
                        url_mysql = prop.get("url_mysql").toString();
                    }
                    if (CodeUtil.isNotEmpty(prop.get("url_oracle"))) {
                        url_oracle = prop.get("url_oracle").toString();
                    }
                    if (CodeUtil.isNotEmpty(prop.get("username"))) {
                        username = prop.get("username").toString();
                    }
                    if (CodeUtil.isNotEmpty(prop.get("password"))) {
                        password = prop.get("password").toString();
                    }
                    if (CodeUtil.isNotEmpty(prop.get("path"))) {
                        path = prop.get("path").toString();
                    }
                    if (CodeUtil.isNotEmpty(prop.get("author"))) {
                        author = prop.get("author").toString();
                    }
                    is.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 更新配置
    public void updateConfig(String driver, String url, String username, String password, String path, String author) {
        try {
            if (configFile.exists() && prop != null) {
                prop.setProperty("driver", driver);
                if (driver.contains("oracle")) {
                    prop.setProperty("url_oracle", url);
                } else {
                    prop.setProperty("url_mysql", url);
                }
                prop.setProperty("username", username);
                prop.setProperty("password", password);
                prop.setProperty("path", path);
                prop.setProperty("author", author);
                OutputStream fos = new FileOutputStream(new File(configPath));
                prop.store(fos, "update date: ");
                fos.flush();
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 获取url
    public String getUrl(int type) {
        String url;
        if (type == 1) {
            url = url_mysql;
        } else {
            url = url_oracle;
        }
        return url;
    }
}
