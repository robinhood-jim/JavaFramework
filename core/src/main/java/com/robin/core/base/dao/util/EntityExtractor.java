package com.robin.core.base.dao.util;

import com.robin.core.base.model.BaseObject;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.lob.LobHandler;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EntityExtractor implements
        RowMapper<List<? extends BaseObject>> {
    private Class<? extends BaseObject> targetclazz;
    private LobHandler lobHandler;

    public EntityExtractor(Class<? extends BaseObject> targetclazz,LobHandler lobHandler) {
        this.targetclazz = targetclazz;
        this.lobHandler=lobHandler;
    }

    public List<? extends BaseObject> mapRow(ResultSet rs, int colpos)
            throws SQLException, DataAccessException {
        ResultSetMetaData rsmd = rs.getMetaData();
        List<BaseObject> retList = new ArrayList<BaseObject>();
        int count = rsmd.getColumnCount();
        List<String> columnNameList = new ArrayList<String>();
        String[] typeName = new String[count];
        String[] className = new String[count];
        for (int k = 0; k < count; k++) {
            columnNameList.add(rsmd.getColumnLabel(k + 1));
            typeName[k] = rsmd.getColumnTypeName(k + 1);
            String fullclassName = rsmd.getColumnClassName(k + 1);
            int pos = fullclassName.lastIndexOf(".");
            className[k] = fullclassName.substring(pos + 1).toUpperCase();
        }
        try {
            Field[] fields = targetclazz.getDeclaredFields();
            while (rs.next()) {
                BaseObject tmpobj = targetclazz.newInstance();
                for (int i = 0; i < fields.length; i++) {
                    String columnName = fields[i].getName();
                    if (columnNameList.contains(columnName)) {
                        int pos = columnNameList.indexOf(columnName);
                        if (!"CLOB".equals(className[pos]) && !"BLOB".equals(className[pos])) {
                            Object obj = rs.getObject(columnName);
                            BeanUtils.copyProperty(tmpobj, columnName, obj);
                        } else if ("CLOB".equals(className[pos])) {
                            String result = lobHandler.getClobAsString(rs, pos + 1);
                            BeanUtils.copyProperty(tmpobj, columnName, result);
                        } else {
                            byte[] bytes = lobHandler.getBlobAsBytes(rs, pos + 1);
                            BeanUtils.copyProperty(tmpobj, columnName, bytes);
                        }
                    }
                }
                retList.add(tmpobj);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return retList;
    }
}
