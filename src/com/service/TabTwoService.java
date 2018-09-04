package com.service;

import com.util.CodeUtil;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.Set;

public class TabTwoService {
    public TabTwoService() {
    }

    /*
     * 选文件夹
     */
    public File chooseFolder(String path) {
        Stage fileStage = null;
        DirectoryChooser folderChooser = new DirectoryChooser();
        folderChooser.setTitle("Choose Folder");
        File oldFolder = new File(path);
        if (oldFolder.isDirectory()) {
            folderChooser.setInitialDirectory(oldFolder);
        }
        File selectedFile = folderChooser.showDialog(fileStage);
        return selectedFile;
    }

    /*
     * 连接数据库
     */
    public CodeUtil connectDb(String driverClass, String url, String name, String pw) {
        CodeUtil codeUtil = new CodeUtil(driverClass, url, name, pw);
        int ret = codeUtil.connect();
        codeUtil.queryTable();
        if (ret == 1) {
            return codeUtil;
        } else {
            return null;
        }
    }

    /*
     * 连接数据库
     */
    public boolean createCode(CodeUtil codeUtil, Set<String> selectedNames, Set<Integer> classTypes, String author, int annotationType, String path) {
        boolean isCreated = false;
        if (codeUtil != null && codeUtil.isConnect()) {
            codeUtil.setAuthor(author);
            codeUtil.setAnnotationType(annotationType);
            codeUtil.setFilePath(path);
            for (String tableName : selectedNames) {
                codeUtil.setTableName(tableName);
                codeUtil.init();
                if (classTypes.contains(0)) {
                    codeUtil.generateController();
                }
                if (classTypes.contains(1)) {
                    codeUtil.generateManager();
                }
                if (classTypes.contains(2)) {
                    codeUtil.generateDao();
                }
                if (classTypes.contains(3)) {
                    codeUtil.generateModel();
                }
            }
            isCreated = true;
        }
        return isCreated;
    }

    /*
     * 筛选表
     */
    public List<String> listTableNames(CodeUtil codeUtil, String tableName) {
        return codeUtil.allTableNames;
    }
}
