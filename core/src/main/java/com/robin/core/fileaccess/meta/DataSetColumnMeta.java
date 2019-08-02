package com.robin.core.fileaccess.meta;

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
public class DataSetColumnMeta{
    private String columnName;
    private String columnType;
    private Object defaultNullValue;
    private boolean required;
    protected DataSetColumnMeta(String columnName,String columnType,Object defaultNullValue){
        this.columnName=columnName;
        this.columnType=columnType;
        if(defaultNullValue!=null){
            this.defaultNullValue=defaultNullValue;
        }else{
            this.defaultNullValue="";
        }
    }
    protected DataSetColumnMeta(String columnName,String columnType,Object defaultNullValue,boolean required){
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
}