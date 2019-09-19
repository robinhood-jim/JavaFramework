package com.robin.core.fileaccess.meta;

import java.io.Serializable;

/**
 * <p>Project:  frame</p>
 * <p>
 * <p>Description:com.robin.core.fileaccess.meta</p>
 * <p>
 * <p>Copyright: Copyright (c) 2019 create at 2019年08月02日</p>
 * <p>
 * <p>Company: zhcx_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
public class DataSetColumnMeta implements Serializable {
    private String columnName;
    private String columnType;
    private Object defaultNullValue;
    private String dateFormat;
    private boolean required;
    private boolean algrithColumn;
    private String algrithOper;

    public DataSetColumnMeta(String columnName,String columnType,Object defaultNullValue){
        this.columnName=columnName;
        this.columnType=columnType;
        if(defaultNullValue!=null){
            this.defaultNullValue=defaultNullValue;
        }else{
            this.defaultNullValue="";
        }
    }
    public DataSetColumnMeta(String columnName,String columnType,Object defaultNullValue,boolean required,String dateFormat){
        this.columnName=columnName;
        this.columnType=columnType;
        if(defaultNullValue!=null){
            this.defaultNullValue=defaultNullValue;
        }else{
            this.defaultNullValue="";
        }
        this.required=required;
        this.dateFormat=dateFormat;
    }
    public DataSetColumnMeta(String columnName,String columnType,Object defaultNullValue,boolean required){
        this.columnName=columnName;
        this.columnType=columnType;
        if(defaultNullValue!=null){
            this.defaultNullValue=defaultNullValue;
        }else{
            this.defaultNullValue="";
        }
        this.required=required;
    }
    public String getColumnName() {
        return columnName;
    }
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }
    public String getColumnType() {
        return columnType;
    }
    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }
    public Object getDefaultNullValue() {
        return defaultNullValue;
    }
    public void setDefaultNullValue(Object defaultNullValue) {
        this.defaultNullValue = defaultNullValue;
    }

    public boolean isRequired() {
        return required;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isAlgrithColumn() {
        return algrithColumn;
    }

    public void setAlgrithColumn(boolean algrithColumn) {
        this.algrithColumn = algrithColumn;
    }

    public String getAlgrithOper() {
        return algrithOper;
    }

    public void setAlgrithOper(String algrithOper) {
        this.algrithOper = algrithOper;
    }
}