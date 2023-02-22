package com.robin.core.query.extractor;

import com.robin.core.fileaccess.meta.DataMappingMeta;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.util.Map;


public class ResultSetExtractorUtils {
    private ResultSetExtractorUtils(){

    }
    public static void wrapResultSetToMap(ResultSet rs, String encode, Map<String, Object> map) throws SQLException {
        String columnName;
        Assert.notNull(rs,"");
        ResultSetMetaData rsmd=rs.getMetaData();
        int recordCount = rsmd.getColumnCount();
        for (int i = 0; i < recordCount; i++) {
            columnName = rsmd.getColumnLabel(i + 1);
            int columnType = rsmd.getColumnType(i + 1);
            map.put(columnName,getValueByMeta(rs,i,columnType,encode));
        }

    }
    public static void wrapResultSetByMapping(ResultSet rs, String encode, DataMappingMeta mappingMeta, Map<String, Object> valueMap) throws SQLException{
        Assert.notNull(rs,"");
        ResultSetMetaData rsmd=rs.getMetaData();
        int recordCount = rsmd.getColumnCount();
        String columnName;
        String displayName;
        for (int i = 0; i < recordCount; i++) {
            columnName=rsmd.getColumnLabel(i+1);
            displayName=columnName;
            if(mappingMeta.getMappingMap().containsKey(columnName)){
                displayName=mappingMeta.getMappingMap().get(columnName);
            }
            int columnType = rsmd.getColumnType(i + 1);
            valueMap.put(displayName,getValueByMeta(rs,i,columnType,encode));
        }
    }
    private static Object getValueByMeta(ResultSet rs,int i,int columnType,String encode) throws SQLException{
        Object retObj=null;
        if (Types.DATE == columnType) {
            retObj=rs.getDate(i + 1);

        } else if (Types.TIMESTAMP == columnType || Types.TIME == columnType) {
            retObj=rs.getTimestamp(i + 1);
        } else if (Types.INTEGER == columnType) {
            retObj=rs.getInt(i + 1);
        } else if (Types.SMALLINT == columnType) {
            retObj=rs.getShort(i + 1);
        } else if (Types.BIGINT == columnType) {
            retObj=rs.getLong(i + 1);
        } else if (Types.FLOAT == columnType) {
            retObj=rs.getFloat(i + 1);
        } else if (Types.DOUBLE == columnType) {
            retObj=rs.getDouble(i + 1);
        } else if (Types.VARCHAR == columnType || Types.CHAR == columnType || Types.NVARCHAR == columnType || Types.LONGVARCHAR == columnType) {
            String msg=rs.getString(i+1);
            if(!StringUtils.isEmpty(encode) && !StringUtils.isEmpty(msg)){
                try {
                    msg = new String(msg.getBytes(), encode);
                }catch (UnsupportedEncodingException ex){

                }
            }
            return msg;
        } else if (Types.DECIMAL == columnType) {
            return rs.getBigDecimal(i + 1);
        } else if (Types.CLOB == columnType || Types.BLOB == columnType || Types.BINARY == columnType || Types.JAVA_OBJECT == columnType) {
            return rs.getBytes(i + 1);
        } else {
            retObj=rs.getObject(i + 1);
        }
        return retObj;
    }
}
