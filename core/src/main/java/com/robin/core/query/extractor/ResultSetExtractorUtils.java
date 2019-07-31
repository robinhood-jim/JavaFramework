package com.robin.core.query.extractor;

import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.util.Map;

/**
 * <p>Project:  frame</p>
 * <p>
 * <p>Description:com.robin.core.query.extractor</p>
 * <p>
 * <p>Copyright: Copyright (c) 2019 create at 2019年07月31日</p>
 * <p>
 * <p>Company: zhcx_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
public class ResultSetExtractorUtils {
    public static void wrappResultSetToMap(ResultSet rs, ResultSetMetaData rsmd, String encode, Map<String,Object> map) throws SQLException{
        Object obj;
        Date date;
        Timestamp stamp;
        String result;
        String columnName ;
        String typeName;
        String className;
        int recordCount=rsmd.getColumnCount();
        for (int i = 0; i < recordCount; i++) {
            obj = rs.getObject(i + 1);
            columnName=rsmd.getColumnLabel(i+1);
            typeName=rsmd.getColumnTypeName(i+1);
            String fullclassName = rsmd.getColumnClassName(i + 1);
            int pos = fullclassName.lastIndexOf(".");
            className= fullclassName.substring(pos + 1).toUpperCase();
            if (rs.wasNull()) {
                map.put(columnName, "");
            } else if (typeName.equalsIgnoreCase("DATE")) {
                date = rs.getDate(i + 1);
                map.put(columnName, date);
            } else if (typeName.equalsIgnoreCase("TIMESTAMP") || typeName.equalsIgnoreCase("datetime")) {
                stamp = rs.getTimestamp(i + 1);
                map.put(columnName, stamp);
            } else if (className.contains("CLOB")) {
                try {
                    result = new String(rs.getBytes(i + 1), encode);
                    map.put(columnName, result);
                } catch (UnsupportedEncodingException ex) {
                    throw new SQLException(ex);
                }
            } else if (className.contains("BLOB") || className.equals("OBJECT")) {
                obj = rs.getBytes(i + 1);
                map.put(columnName, obj);
            } else
                map.put(columnName, obj);
        }

    }
}
