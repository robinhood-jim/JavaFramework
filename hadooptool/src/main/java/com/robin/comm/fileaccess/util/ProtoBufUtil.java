package com.robin.comm.fileaccess.util;

import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;


public class ProtoBufUtil {
    public static String translateType(DataSetColumnMeta column) {
        String retStr = "";
        if(column.getColumnType().equals(Const.META_TYPE_INTEGER)){
            retStr="int32";
        } else if (column.getColumnType().equals(Const.META_TYPE_BIGINT)) {
            retStr = "int64";
        } else if (column.getColumnType().equals(Const.META_TYPE_DOUBLE)) {
            retStr = "double";
        } else if (column.getColumnType().equals(Const.META_TYPE_NUMERIC)) {
            retStr = "float";
        } else if (column.getColumnType().equals(Const.META_TYPE_TIMESTAMP)) {
            retStr = "int64";
        } else if (column.getColumnType().equals(Const.META_TYPE_BOOLEAN)) {
            retStr = "bool";
        } else if (column.getColumnType().equals(Const.META_TYPE_STRING) ||column.getColumnType().equals(Const.META_TYPE_CLOB)) {
            retStr = "string";
        } else if (column.getColumnType().equals(Const.META_TYPE_BINARY) || column.getColumnType().equals(Const.META_TYPE_BLOB)) {
            retStr = "byte";
        }
        return retStr;
    }
}
