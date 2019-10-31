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

    public void doOperationInQuery(Session querysession, String sql, Object[] params) {
        PreparedStatement statement = querysession.prepare(sql);
        Statement bindstmt = statement.bind(params);
        ResultSet rs = querysession.execute(bindstmt);
        Iterator<Row> iter = rs.iterator();
        while (iter.hasNext()) {
            doInQuery(iter.next());
        }
    }

    protected void doInQuery(Row row) throws RuntimeException {
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
            if (type == DataType.bigint()) {
                value = row.getLong(columnName);
            } else if (type == DataType.cboolean()) {
                value = row.getBool(columnName);
            } else if (type == DataType.blob()) {
                value = row.getBytes(columnName);
            } else if (type == DataType.timestamp()) {
                value = row.getDate(columnName);
            } else if (type == DataType.decimal()) {
                value = row.getDecimal(columnName);
            } else if (type == DataType.cfloat()) {
                value = row.getFloat(columnName);
            } else if (type == DataType.inet()) {
                value = row.getInet(columnName);
            } else if (type == DataType.cint()) {
                value = row.getInt(columnName);
            }
            /*else if (type.isCollection() && type.asJavaClass() == List.class) {
                value = getCollectionData(row, type, columnName, valClass);
            } else if (type.isCollection() && type.asJavaClass() == Set.class) {
                value = getCollectionData(row, type, columnName, valClass);
            } else if (type.isCollection() && type.asJavaClass() == Map.class) {
                value = getCollectionData(row, type, columnName, valClass, keyClass);
            } */
            else if (type == DataType.varchar()) {
                value = row.getString(columnName);
            } else if (type == DataType.uuid() || type == DataType.timeuuid()) {
                value = row.getUUID(columnName);
            } else if (type == DataType.varint()) {
                value = row.getVarint(columnName);
            } else if (type == DataType.cdouble()) {
                value = row.getDouble(columnName);
            } else if (type == DataType.text()) {
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
