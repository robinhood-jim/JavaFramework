package com.robin.nosql.cassandra;

import com.datastax.driver.core.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Slf4j
public abstract class AbstractCassandraOperation {
    protected int batchSize = 10000;
    protected int rowPos;
    protected Map<String, Object> map = new LinkedHashMap<>();

    public void doOperationInQuery(Session querySession, String sql, Object[] params) {
        PreparedStatement statement = querySession.prepare(sql);
        Statement bindstmt = statement.bind(params);
        ResultSet rs = querySession.execute(bindstmt);
        Iterator<Row> iter = rs.iterator();
        while (iter.hasNext()) {
            doInQuery(iter.next());
        }
    }

    protected void doInQuery(Row row)  {
        List<ColumnDefinitions.Definition> definitions = row.getColumnDefinitions().asList();
        map.clear();
        for (ColumnDefinitions.Definition def : definitions) {
            map.put(def.getName(), wrapValueWithType(def.getType(), def.getName(), row));
        }
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    protected Object wrapValueWithType(DataType type, String columnName, Row row) {
        Object value = null;

        try {
            if (DataType.bigint().equals(type)) {
                value = row.getLong(columnName);
            } else if (DataType.cboolean().equals(type)) {
                value = row.getBool(columnName);
            } else if (DataType.blob().equals(type)) {
                value = row.getBytes(columnName);
            } else if (DataType.timestamp().equals(type)) {
                value = row.getDate(columnName);
            } else if (DataType.decimal().equals(type)) {
                value = row.getDecimal(columnName);
            } else if (DataType.cfloat().equals(type)) {
                value = row.getFloat(columnName);
            } else if (DataType.inet().equals(type)) {
                value = row.getInet(columnName);
            } else if (DataType.cint().equals(type)) {
                value = row.getInt(columnName);
            }
            /*else if (type.isCollection() && type.asJavaClass() == List.class) {
                value = getCollectionData(row, type, columnName, valClass);
            } else if (type.isCollection() && type.asJavaClass() == Set.class) {
                value = getCollectionData(row, type, columnName, valClass);
            } else if (type.isCollection() && type.asJavaClass() == Map.class) {
                value = getCollectionData(row, type, columnName, valClass, keyClass);
            } */
            else if (DataType.varchar().equals(type)) {
                value = row.getString(columnName);
            } else if (DataType.uuid().equals(type) || DataType.timeuuid().equals(type)) {
                value = row.getUUID(columnName);
            } else if (DataType.varint().equals(type)) {
                value = row.getVarint(columnName);
            } else if (DataType.cdouble().equals(type)) {
                value = row.getDouble(columnName);
            } else if (DataType.text().equals(type)) {
                value = row.getString(columnName);
            }

        } catch (Exception e) {
            log.error("获取{}值发生异常：", columnName, e);
        }

        if (value == null) {
            log.error("Column '{}' Type({}) get cassandra data is NULL.", columnName, type);
        }

        return value;
    }

    public int getRowPos() {
        return rowPos;
    }

}
