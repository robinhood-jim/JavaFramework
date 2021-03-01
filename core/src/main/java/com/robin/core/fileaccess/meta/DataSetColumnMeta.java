package com.robin.core.fileaccess.meta;

import lombok.Data;

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
@Data
public class DataSetColumnMeta implements Serializable {
    private String columnName;
    private String columnType;
    private Object defaultNullValue;
    private String dateFormat;
    private boolean required;
    private boolean algrithColumn;
    private boolean primary;
    private String algrithOper;
    private Integer precise;
    private Integer scale;
    private boolean increment;
    private Integer length;

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

}