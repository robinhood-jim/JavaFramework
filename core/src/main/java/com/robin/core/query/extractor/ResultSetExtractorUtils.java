package com.robin.core.query.extractor;

import java.sql.*;
import java.util.Map;


public class ResultSetExtractorUtils {
    public static void wrappResultSetToMap(ResultSet rs, ResultSetMetaData rsmd, String encode, Map<String, Object> map) throws SQLException {
        Object obj;
        Date date;
        Timestamp stamp;
        String result;
        String columnName;
        String typeName;
        String className;
        int recordCount = rsmd.getColumnCount();
        for (int i = 0; i < recordCount; i++) {
            obj = rs.getObject(i + 1);
            columnName = rsmd.getColumnLabel(i + 1);
            int columnType = rsmd.getColumnType(i + 1);

            if (rs.wasNull()) {
                map.put(columnName, null);
            } else if (Types.DATE == columnType) {
                date = rs.getDate(i + 1);
                map.put(columnName, date);
            } else if (Types.TIMESTAMP == columnType || Types.TIME == columnType) {
                stamp = rs.getTimestamp(i + 1);
                map.put(columnName, stamp);
            } else if (Types.INTEGER == columnType) {
                map.put(columnName, rs.getInt(i + 1));
            } else if (Types.SMALLINT == columnType) {
                map.put(columnName, rs.getShort(i + 1));
            } else if (Types.BIGINT == columnType) {
                map.put(columnName, rs.getLong(i + 1));
            } else if (Types.FLOAT == columnType) {
                map.put(columnName, rs.getFloat(i + 1));
            } else if (Types.DOUBLE == columnType) {
                map.put(columnName, rs.getDouble(i + 1));
            } else if (Types.VARCHAR == columnType || Types.CHAR == columnType || Types.NVARCHAR == columnType || Types.LONGVARCHAR == columnType) {
                map.put(columnName, rs.getString(i + 1));
            } else if (Types.DECIMAL == columnType) {
                map.put(columnName, rs.getBigDecimal(i + 1));
            } else if (Types.CLOB == columnType || Types.BLOB == columnType || Types.BINARY == columnType || Types.JAVA_OBJECT == columnType) {
                map.put(columnName, rs.getBytes(i + 1));
            } else {
                map.put(columnName, obj);
            }
        }

    }
}
