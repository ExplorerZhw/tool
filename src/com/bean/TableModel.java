package com.bean;

import com.util.CodeUtil;

/**
 * Created by Administrator on 2018/1/30.
 */
public class TableModel {
    private String tName;
    private String colName;
    private String colType;
    private String colLength;
    private String colScale;
    private String colComments;
    private String javaFieldName;
    private String javaClassName;

    public TableModel() {

    }

    public TableModel(String colName, String colType, String colLength, String colScale, String colComments) {
        this.colName = colName;
        this.colType = colType;
        this.colLength = colLength;
        this.colScale = colScale;
        this.colComments = colComments;
        this.javaFieldName = CodeUtil.toJavaFieldName(colName);
        this.javaClassName = CodeUtil.toJavaClassName(colName);
    }

    public String gettName() {
        return tName;
    }

    public void settName(String tName) {
        this.tName = tName;
    }

    public String getColName() {
        return colName;
    }

    public void setColName(String colName) {
        this.colName = colName;
    }

    public String getColType() {
        return colType;
    }

    public void setColType(String colType) {
        this.colType = colType;
    }

    public String getColLength() {
        return colLength;
    }

    public void setColLength(String colLength) {
        this.colLength = colLength;
    }

    public String getColScale() {
        return colScale;
    }

    public void setColScale(String colScale) {
        this.colScale = colScale;
    }

    public String getColComments() {
        return colComments;
    }

    public void setColComments(String colComments) {
        this.colComments = colComments;
    }

    public String getJavaFieldName() {
        return javaFieldName;
    }

    public void setJavaFieldName(String javaFieldName) {
        this.javaFieldName = javaFieldName;
    }

    public String getJavaClassName() {
        return javaClassName;
    }

    public void setJavaClassName(String javaClassName) {
        this.javaClassName = javaClassName;
    }
}
