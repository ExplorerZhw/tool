package com.controller;

import com.service.TabOneService;
import com.service.TabThreeService;
import com.service.TabTwoService;
import com.util.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainController {
    // tab 1
    @FXML
    private TextArea oldText;
    @FXML
    private TextArea newText;
    // tab 2
    @FXML
    private TextField filePathTf;
    @FXML
    private Label conState;
    @FXML
    private TextField dbDriver;
    @FXML
    private TextField dbUrl;
    @FXML
    private TextField dbName;
    @FXML
    private TextField dbPs;
    @FXML
    private Button changeDbType;
    @FXML
    private CheckBox controllerBox;
    @FXML
    private CheckBox serviceBox;
    @FXML
    private CheckBox daoBox;
    @FXML
    private CheckBox modelBox;
    @FXML
    private TextField authorTf;
    @FXML
    private ChoiceBox annotationType;
    @FXML
    private TextField tableNameTf;
    @FXML
    private ListView tableNameLv1;
    @FXML
    private ListView tableNameLv2;

    // tab 3
    @FXML
    private TextArea oracleText;
    @FXML
    private TextArea mysqlText;

    public FrameUtil frameUtil = new FrameUtil();
    public ConfigUtil configUtil = new ConfigUtil();
    public CodeUtil codeUtil;
    public FileUtil fileUtil = new FileUtil();
    private Set<String> selectedNameSet = new HashSet<>();

    /*
     * 格式化json
     */
    @FXML
    public void parseJsonAct(ActionEvent event) {
        String oldStr = oldText.getText();
        String newStr = new TabOneService().parseJson(oldStr);
        newText.setText(newStr);
    }

    /*
     * 选文件夹按钮
     */
    @FXML
    public void chooseFolderAct(ActionEvent event) {
        File folderFile = new TabTwoService().chooseFolder(filePathTf.getText());
        if (folderFile != null) {
            String path = folderFile.getPath();
            if (StringUtils.isNotEmpty(path)) {
                if (!path.endsWith("\\")) {
                    path += "\\";
                }
                Util util = new Util();
                path += util.formatDate(new Date(), util.FORMAT_MD) + util.milTime();
                filePathTf.setText(path);
            }
        }
    }

    /*
     * 数据库连接按钮
     */
    @FXML
    public void conDataBaseAct(ActionEvent event) {
        String driverClass = dbDriver.getText();
        String url = dbUrl.getText();
        String name = dbName.getText();
        String pw = dbPs.getText();
        codeUtil = new TabTwoService().connectDb(driverClass, url, name, pw);
        if (codeUtil != null) {
            conState.setText("连接状态：连接");
            String path = filePathTf.getText();
            Pattern pattern = Pattern.compile("\\d+$");
            Matcher matcher = pattern.matcher(path);
            if (matcher.find()) {
                path = path.substring(0, path.lastIndexOf("\\") + 1);
                Util util = new Util();
                path += util.formatDate(new Date(), util.FORMAT_MD) + util.milTime();
            }
            String author = authorTf.getText();
            configUtil.updateConfig(driverClass, url, name, pw, path, author);
            configUtil.setFileLoadState(0);
        } else {
            frameUtil.alertInfo("错误：", "连接失败");
        }
    }

    /*
     * 加载配置文件
     */
    @FXML
    public void loadProperties() {
        int dbType = 0;
        if (configUtil.getFileLoadState() == 0) {
            configUtil.initConfig();
        }
        filePathTf.setText(configUtil.path);
        dbDriver.setText(configUtil.driver);
        if (!configUtil.driver.contains("oracle")) {
            dbType = 1;
        }
        dbUrl.setText(configUtil.getUrl(dbType));
        dbName.setText(configUtil.username);
        dbPs.setText(configUtil.password);
        authorTf.setText(configUtil.author);
        changeDbType.onActionProperty();
        configUtil.setFileLoadState(1);
    }

    /*
     * 切换数据库类型
     */
    @FXML
    public void changeDriver(ActionEvent event) {
        String oracleDriver = "oracle.jdbc.driver.OracleDriver";
        String mysqlDriver = "com.mysql.jdbc.Driver";
        String driver;
        int dbType;
        if (dbDriver.getText() != null && dbDriver.getText().contains("oracle")) {
            driver = mysqlDriver;
            dbType = 1;
        } else {
            driver = oracleDriver;
            dbType = 0;
        }
        dbUrl.setText(configUtil.getUrl(dbType));
        dbDriver.setText(driver);
    }

    /*
     * 生成Java类
     */
    @FXML
    public void createCode(ActionEvent event) {
        Set<Integer> classTypes = new HashSet<>();
        if (controllerBox.isSelected()) {
            classTypes.add(0);
        }
        if (serviceBox.isSelected()) {
            classTypes.add(1);
        }
        if (daoBox.isSelected()) {
            classTypes.add(2);
        }
        if (modelBox.isSelected()) {
            classTypes.add(3);
        }
        if (classTypes.size() < 1) {
            frameUtil.alertInfo("", "请选择至少一种JAVA CLASS");
            return;
        }
        if (selectedNameSet.size() < 1) {
            frameUtil.alertInfo("", "请选择至少一个数据表");
            return;
        }
        String authorStr = authorTf.getText();
        String annotationTypeStr = annotationType.getSelectionModel().getSelectedItem().toString();
        int annotationType = 0;
        if ("Swagger2".equalsIgnoreCase(annotationTypeStr)) {
            annotationType = 1;
        }
        String path = filePathTf.getText();
        boolean flag = false;
        if (StringUtils.isEmpty(path)) {
            frameUtil.alertInfo("", "请选择文件存放路径");
        } else {
            flag = new TabTwoService().createCode(codeUtil, selectedNameSet, classTypes, authorStr, annotationType, path);
        }
        if (flag) {
            frameUtil.alertInfo("", "创建成功！");
            fileUtil.openFilePath(path);
        } else {
            frameUtil.alertInfo("", "创建失败！");
        }
    }

    /*
     * 依据输入内容查询表名
     */
    @FXML
    public void changeTableName(ActionEvent event) {
        String tableName = tableNameTf.getText();
        ObservableList<String> list = FXCollections.observableArrayList();
        List<String> allNames = new TabTwoService().listTableNames(codeUtil, tableName);
        for (String name : allNames) {
            if (name.contains(tableName.toUpperCase())) {
                list.add(name);
            }
        }
        tableNameLv1.setItems(list); //tableview添加list
    }

    /*
     * 点击待选区内容事件
     */
    @FXML
    public void clickLv1(Event event) {
        ObservableList<String> list = FXCollections.observableArrayList();
        String tableName;
        if (tableNameLv1.getSelectionModel().getSelectedItem() != null) {
            tableName = tableNameLv1.getSelectionModel().getSelectedItem().toString();
            selectedNameSet.add(tableName);
            for (String name : selectedNameSet) {
                list.add(name);
            }
            tableNameLv2.setItems(list); //tableview添加list
        }
    }

    /*
     * 点击已选区内容事件
     */
    @FXML
    public void clickLv2(Event event) {
        ObservableList<String> list = FXCollections.observableArrayList();
        if (tableNameLv2.getSelectionModel().getSelectedItem() != null) {
            String tableName = tableNameLv2.getSelectionModel().getSelectedItem().toString();
            Set<String> tempSet = new HashSet<>();
            if (StringUtils.isNotEmpty(tableName) && selectedNameSet.size() > 0) {
                for (String name : selectedNameSet) {
                    if (name.equals(tableName)) {
                        continue;
                    }
                    tempSet.add(name);
                    list.add(name);
                }
                selectedNameSet = tempSet;
                tableNameLv2.setItems(list); //tableview添加list
            }
        }
    }

    /*
     * insert MySQL转Oracle
     */
    @FXML
    public void toOracle(ActionEvent event) {
        String mysql = mysqlText.getText();
        String oracle = new TabThreeService().toOracle(mysql);
        oracleText.setText(oracle);
    }

    /*
     * insert Oracle转MySQL
     */
    @FXML
    public void toMysql(ActionEvent event) {
        String oracle = oracleText.getText();
        String mysql = new TabThreeService().toMysql(oracle);
        mysqlText.setText(mysql);
    }
}
