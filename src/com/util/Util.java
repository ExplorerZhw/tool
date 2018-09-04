package com.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {
    public static final String FORMAT_FULL = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_MD = "MMdd_";

    // 格式化日期
    public String formatDate(Date date, String pattern) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }

    // 毫秒数
    public String milTime() {
        long mt = System.currentTimeMillis();
        return String.valueOf(mt);
    }
}
