/*
 * Copyright (c) 2015,robinjim(robinjim@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.robin.core.base.dao;

import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.robin.core.base.dao.util.AnnotationRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;

import com.robin.core.base.model.BaseObject;

public class LobCreatingPreparedStatementCallBack extends
        AbstractLobCreatingPreparedStatementCallback {
    private BaseObject obj;
    private List<AnnotationRetriever.FieldContent> fields;
    private Logger logger= LoggerFactory.getLogger(getClass());

    public LobCreatingPreparedStatementCallBack(LobHandler lobHandler) {
        super(lobHandler);

    }

    public LobCreatingPreparedStatementCallBack(LobHandler lobHandler, List<AnnotationRetriever.FieldContent> fields, BaseObject object) {
        super(lobHandler);
        this.obj = object;
        this.fields = fields;
    }


    public BaseObject getObj() {
        return obj;
    }


    @Override
    protected void setValues(PreparedStatement ps, LobCreator lobCreator)
            throws SQLException, DataAccessException {
        int pos = 1;
        try {
            for (AnnotationRetriever.FieldContent field : fields) {
                Object value = field.getGetMethod().invoke(obj, null);
                if (!field.isIncrement() && !field.isSequential() && value != null) {
                    boolean needDo = true;
                    if (field.isPrimary() && field.getPrimaryKeys() != null) {
                        needDo = false;
                        for (AnnotationRetriever.FieldContent fieldContent : field.getPrimaryKeys()) {
                            setValueByDataType(ps, value, lobCreator, field.getDataType(), pos);
                            pos++;
                        }
                    }
                    if (needDo) {
                        setValueByDataType(ps, value, lobCreator, field.getDataType(), pos);
                        pos++;
                    }
                }

            }
        } catch (IllegalAccessException ex) {

        } catch (InvocationTargetException ex1) {

        }

    }

    private void setValueByDataType(PreparedStatement ps, Object value, LobCreator lobCreator, String dataType, int pos) throws SQLException {
        if ("clob".equalsIgnoreCase(dataType)) {
            lobCreator.setClobAsString(ps, pos, value.toString());
        } else if ("blob".equalsIgnoreCase(dataType)) {
            lobCreator.setBlobAsBytes(ps, pos, (byte[]) value);
        } else {
            setValue(ps, pos, value);
        }
    }

    private void setValue(PreparedStatement ps, int pos, Object value) {
        try {
            Class clazz = value.getClass();
            if (clazz.equals(Long.class)) {
                ps.setLong(pos, (Long) value);
            } else if (clazz.equals(Integer.class)) {
                ps.setInt(pos, Integer.parseInt(value.toString()));
            } else if (clazz.equals(String.class)) {
                ps.setString(pos, value.toString());
            } else if (clazz.equals(Date.class)) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Timestamp date = new Timestamp(format.parse(value.toString()).getTime());
                ps.setTimestamp(pos, date);
            } else if (clazz.equals(Timestamp.class)) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Timestamp date = new Timestamp(format.parse(value.toString()).getTime());
                ps.setTimestamp(pos, date);
            } else if (clazz.equals(java.sql.Date.class)) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                java.sql.Date date = new java.sql.Date(format.parse(value.toString()).getTime());
                ps.setDate(pos, date);
            } else if (clazz.equals(Double.class)) {
                ps.setDouble(pos, Double.valueOf(value.toString()));
            } else if (clazz.equals(Float.class)) {
                ps.setFloat(pos, Float.valueOf(value.toString()));
            }
        } catch (Exception e) {
            logger.error("",e);
        }

    }

}
