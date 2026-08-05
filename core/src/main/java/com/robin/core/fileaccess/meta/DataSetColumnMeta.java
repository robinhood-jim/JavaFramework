package com.robin.core.fileaccess.meta;

import com.robin.core.base.datameta.DataBaseColumnMeta;
import com.robin.core.base.util.Const;
import lombok.Data;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.util.List;


@Data
public class DataSetColumnMeta implements Serializable {
    private String columnName;
    private String columnCode;
    private Const.CLASSTYPE columnType;
    private Object defaultNullValue;
    private String dateFormat;
    private Integer dataType;
    private boolean required;
    private boolean algrithColumn;
    private boolean primary;
    private String algrithOper;
    private Integer precise;
    private Integer scale;
    private boolean increment;
    private Integer length;
    private List<String> nominalValues;
    private boolean flushOut=true;
    public DataSetColumnMeta(){

    }

    public DataSetColumnMeta(String columnName, Const.CLASSTYPE columnType, Object defaultNullValue){
        this.columnName=columnName;
        this.columnCode=columnName;
        this.columnType=columnType;
        if(defaultNullValue!=null){
            this.defaultNullValue=defaultNullValue;
        }else{
            this.defaultNullValue="";
        }
    }
    public DataSetColumnMeta(String columnName, String columnCode, Const.CLASSTYPE columnType, Object defaultNullValue){
        this(columnName,columnType,defaultNullValue);
        this.columnCode=columnCode;
    }

    public DataSetColumnMeta(String columnName, Const.CLASSTYPE columnType){
        this(columnName,columnType,null);
    }
    public DataSetColumnMeta(String columnName, Const.CLASSTYPE columnType, String algrithOper, boolean flushOut){
        this(columnName,columnType,null);
        this.algrithOper=algrithOper;
        this.flushOut=flushOut;
    }

    public DataSetColumnMeta(String columnName, Const.CLASSTYPE columnType, boolean flushOut){
        this(columnName,columnType,null);
        this.flushOut=flushOut;
    }

    public DataSetColumnMeta(String columnName, Const.CLASSTYPE columnType, Object defaultNullValue, boolean required, String dateFormat){
        this(columnName,columnType,defaultNullValue);
        this.required=required;
        this.dateFormat=dateFormat;
    }
    public DataSetColumnMeta(String columnName, Const.CLASSTYPE columnType, Object defaultNullValue, boolean required){
        this(columnName,columnType,defaultNullValue);
        this.required=required;
    }
    public DataBaseColumnMeta toDataBaseColumnMeta(){
        DataBaseColumnMeta.Builder builder=DataBaseColumnMeta.Builder.builder();
        builder.columnName(columnName).columnType(columnType);
        if(!ObjectUtils.isEmpty(defaultNullValue)){
            builder.defaultValue(defaultNullValue.toString());
        }
        builder.scale(scale).precise(precise).primaryKey(primary).nullable(!required).increment(increment);
        return builder.build();
    }

}