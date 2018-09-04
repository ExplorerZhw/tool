package com.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class FileUtil {
    // 生成文件
    public void createFile(String content, String className, String packageName, String filePath, String fileName) {
        try {
            File folder = new File(filePath);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File file = new File(filePath + "\\" + fileName
                    .replace("{className}", className)
                    .replace("{packageName}", packageName.replace(".", "\\")));
            if (file.exists()) {
                file.deleteOnExit();
            }
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(fos, "UTF-8"));
            pw.write(content.toCharArray());
            pw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 打开文件路径
    public void openFilePath(String filePath) {
        try {
            Runtime.getRuntime().exec("cmd /c explorer " + filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
