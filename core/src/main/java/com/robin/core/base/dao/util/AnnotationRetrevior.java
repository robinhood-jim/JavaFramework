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
package com.robin.core.base.dao.util;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.exception.DAOException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.model.BasePrimaryObject;
import com.robin.core.base.util.Const;
import org.springframework.jdbc.support.lob.LobHandler;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.sql.Date;
import java.sql.*;
import java.util.*;

public class AnnotationRetrevior {
    private static Map<Class<? extends BaseObject>, Map<String, String>> tableCfgMap = new HashMap<>();
    private static Map<Class<? extends BaseObject>, EntityContent> entityCfgMap = new HashMap<>();
    private static Map<Class<? extends BaseObject>, Map<String, FieldContent>> fieldCfgMap = new HashMap<>();
    private static Map<Class<? extends BaseObject>, List<FieldContent>> fieldListMap = new HashMap<>();


    public static List<FieldContent> getMappingFieldsCache(Class<? extends BaseObject> clazz) throws DAOException {
        List<FieldContent> list = null;
        if (!fieldListMap.containsKey(clazz)) {
            list = getMappingFields(clazz);
            fieldListMap.put(clazz, list);
        } else {
            list = fieldListMap.get(clazz);
        }
        return list;
    }

    public static Map<String, FieldContent> getMappingFieldsMapCache(Class<? extends BaseObject> clazz) throws DAOException {
        Map<String, FieldContent> map = null;
        if (!fieldCfgMap.containsKey(clazz)) {
            map = getMappingFieldsMap(clazz);
            fieldCfgMap.put(clazz, map);
        } else {
            map = fieldCfgMap.get(clazz);
        }
        return map;
    }


    private static List<FieldContent> getMappingFields(Class<? extends BaseObject> clazz) throws DAOException {
        boolean flag = clazz.isAnnotationPresent(MappingEntity.class);
        List<FieldContent> list = new ArrayList<>();
        if (flag) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                MappingField mapfield = field.getAnnotation(MappingField.class);
                if (mapfield != null) {
                    list.add(retrieveField(field, clazz));
                }
            }
        } else {
            flag = clazz.isAnnotationPresent(Entity.class);
            if (flag) {
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    Column mapfield = field.getAnnotation(Column.class);
                    if (mapfield != null) {
                        list.add(retrieveFieldJpa(field, clazz));
                    }
                }
            }
        }
        return list;
    }

    private static Map<String, FieldContent> getMappingFieldsMap(Class<? extends BaseObject> clazz) throws DAOException {
        boolean flag = clazz.isAnnotationPresent(MappingEntity.class);
        Map<String, FieldContent> map = new HashMap<>();
        if (flag) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                MappingField mapfield = field.getAnnotation(MappingField.class);
                if (mapfield != null) {
                    map.put(field.getName(), retrieveField(field, clazz));
                }
            }
        } else {
            flag = clazz.isAnnotationPresent(Entity.class);
            if (flag) {
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    Column mapfield = field.getAnnotation(Column.class);
                    if (mapfield != null) {
                        map.put(field.getName(), retrieveFieldJpa(field, clazz));
                    }
                }
            }
        }
        return map;
    }


    public static EntityContent getMappingTableByCache(Class<? extends BaseObject> clazz) throws DAOException {
        EntityContent content;
        if (!tableCfgMap.containsKey(clazz)) {
            content = getMappingTableEntity(clazz);
            entityCfgMap.put(clazz, content);
        } else {
            content = entityCfgMap.get(clazz);
        }
        return content;
    }

    private static EntityContent getMappingTableEntity(Class<? extends BaseObject> clazz) throws DAOException {

        EntityContent content = null;
        boolean flag = clazz.isAnnotationPresent(MappingEntity.class);
        if (flag) {
            MappingEntity entity = clazz.getAnnotation(MappingEntity.class);
            String tableName = entity.table();
            String schema = entity.schema();
            String jdbcDao = entity.jdbcDao();
            content = getEntityContent(tableName, schema, false);
            if (!jdbcDao.isEmpty()) {
                content.setJdbcDao(jdbcDao);
            }
        } else {
            flag = clazz.isAnnotationPresent(Entity.class);
            if (flag) {
                Table table = clazz.getAnnotation(Table.class);
                String tableName = table.name();
                String schema = table.schema();
                content = getEntityContent(tableName, schema, false);
            } else {
                throw new DAOException("must using MappingEnity or JPA annotation");
            }
        }
        return content;
    }

    private static EntityContent getEntityContent(String tableName, String schema, boolean jpaAnnotation) {
        if (!schema.isEmpty()) {
            if (jpaAnnotation) {
                return new EntityContent(tableName, schema, jpaAnnotation);
            } else {
                return new EntityContent(tableName, schema);
            }
        } else {
            if (jpaAnnotation) {
                return new EntityContent(tableName, jpaAnnotation);
            } else {
                return new EntityContent(tableName);
            }
        }
    }


    public static FieldContent getPrimaryField(List<FieldContent> columList) {
        FieldContent pkField = null;
        for (FieldContent field : columList) {
            if (field.isPrimary()) {
                pkField = field;
                break;
            }
        }
        return pkField;
    }

    public static void validateEntity(BaseObject object) throws DAOException {
        Map<String, FieldContent> fieldsMap = getMappingFieldsMapCache(object.getClass());
        Iterator<Map.Entry<String, FieldContent>> iterator = fieldsMap.entrySet().iterator();
        try {
            while (iterator.hasNext()) {
                Map.Entry<String, FieldContent> entry = iterator.next();
                Object value = entry.getValue().getGetMethod().invoke(object, null);
                if (entry.getValue().isSequential() || entry.getValue().isIncrement()) {
                    break;
                }
                if (entry.getValue().isRequired() && value == null) {
                    throw new DAOException("column " + entry.getKey() + " must not be null!");
                }
                if (entry.getValue().getScale() > 0 || entry.getValue().getPrecise() > 0 && value != null
                        && !(value instanceof Float || value instanceof Double || value instanceof Number)) {
                    throw new DAOException("column " + entry.getKey() + " must digital!");
                }
                if (entry.getValue().getLength() > 0 && value != null && value.toString().length() > entry.getValue().getLength()) {
                    throw new DAOException("column " + entry.getKey() + " is large than max length=" + entry.getValue().getLength());
                }

            }
        } catch (DAOException ex1) {
            throw ex1;
        } catch (Exception ex) {
            throw new DAOException(ex);
        }
    }

    private static FieldContent retrieveFieldJpa(Field field, Class<? extends BaseObject> clazz) throws DAOException {
        FieldContent content = null;
        try {

            String tmname = field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
            Method getMethod = clazz.getMethod("get" + tmname, null);
            Type type = getMethod.getReturnType();
            Method setMethod = clazz.getMethod("set" + tmname, null);

            Column mapfield = field.getAnnotation(Column.class);
            String fieldName;
            if (mapfield != null && mapfield.name() != null && !mapfield.name().isEmpty()) {
                fieldName = mapfield.name();
            } else {
                fieldName = field.getName();
            }
            content = new FieldContent(field.getName(), fieldName, field, getMethod, setMethod);
            Id idfield = field.getAnnotation(Id.class);
            if (idfield != null) {
                content.setPrimary(true);
                GeneratedValue genval = field.getAnnotation(GeneratedValue.class);
                if (genval != null) {
                    if (genval.strategy() == GenerationType.AUTO) {
                        content.setIncrement(true);
                    } else if (genval.strategy() == GenerationType.IDENTITY) {
                        content.setIncrement(true);
                    } else if (genval.strategy() == GenerationType.SEQUENCE) {
                        SequenceGenerator generator = field.getAnnotation(SequenceGenerator.class);
                        if (generator != null) {
                            content.setSequential(true);
                            content.setSequenceName(generator.sequenceName());
                        }
                    }
                }
            }
            if (type.equals(Void.TYPE)) {
            } else if (type.equals(Long.TYPE)) {
                content.setDataType("int");
            } else if (type.equals(Integer.TYPE)) {
                content.setDataType("int");
            } else if (type.equals(Double.TYPE)) {
                content.setDataType("numeric");
            } else if (type.equals(Float.TYPE)) {
                content.setDataType("numeric");
            } else if (type.equals(String.class)) {
                content.setDataType("string");
            } else if (type.equals(java.util.Date.class)) {
                content.setDataType("date");
            } else if (type.equals(Date.class)) {
                content.setDataType("date");
            } else if (type.equals(byte[].class)) {
                content.setDataType("blob");
            } else if (type.equals(Timestamp.class)) {
                content.setDataType("timestamp");
            }
        } catch (Exception ex) {
            throw new DAOException(ex);
        }
        return content;
    }

    private static FieldContent retrieveField(Field field, Class<? extends BaseObject> clazz) throws DAOException {
        FieldContent content = null;
        try {
            MappingField mapfield = field.getAnnotation(MappingField.class);
            String name = field.getName();
            String colname = name.substring(0, 1).toUpperCase() + name.substring(1);

            Method getMethod = clazz.getDeclaredMethod("get" + colname, null);
            Type type = getMethod.getReturnType();
            Method setMethod = clazz.getDeclaredMethod("set" + colname, field.getType());
            if (mapfield != null) {
                String colfield = mapfield.field();
                String datatype = mapfield.datatype();
                String fieldName;
                if (colfield != null && !"".equals(colfield.trim())) {
                    fieldName = colfield;
                } else {
                    fieldName = field.getName();
                }
                content = new AnnotationRetrevior.FieldContent(field.getName(), fieldName, field, getMethod, setMethod);
                if (mapfield.precise() != 0) {
                    content.setPrecise(mapfield.precise());
                }
                if (mapfield.scale() != 0) {
                    content.setScale(mapfield.scale());
                }
                if (mapfield.length() != 0) {
                    content.setLength(mapfield.length());
                }
                if (mapfield.increment()) {
                    content.setIncrement(true);
                }
                if (mapfield.primary()) {
                    content.setPrimary(true);
                    parsePrimaryKey(content, type);
                }
                if (mapfield.sequenceName()!=null && !mapfield.sequenceName().isEmpty()) {
                    content.setSequential(true);
                    content.setSequenceName(mapfield.sequenceName());
                }
                if (datatype == null || "".equals(datatype)) {
                    if (type.equals(Void.class)) {
                    } else if (type.equals(Long.class)) {
                        content.setDataType(Const.META_TYPE_BIGINT);
                    } else if (type.equals(Integer.class)) {
                        content.setDataType(Const.META_TYPE_INTEGER);
                    } else if (type.equals(Double.class)) {
                        content.setDataType(Const.META_TYPE_NUMERIC);
                    } else if (type.equals(Float.class)) {
                        content.setDataType(Const.META_TYPE_NUMERIC);
                    } else if (type.equals(String.class)) {
                        content.setDataType(Const.META_TYPE_STRING);
                    } else if (type.equals(java.util.Date.class)) {
                        content.setDataType(Const.META_TYPE_TIMESTAMP);
                    } else if (type.equals(Date.class)) {
                        content.setDataType(Const.META_TYPE_DATE);
                    } else if (type.equals(byte[].class)) {
                        content.setDataType(Const.META_TYPE_BLOB);
                    } else if (type.equals(Timestamp.class)) {
                        content.setDataType(Const.META_TYPE_TIMESTAMP);
                    } else {
                        content.setDataType(Const.META_TYPE_OBJECT);
                    }
                } else {
                    if ("clob".equalsIgnoreCase(datatype)) {
                        content.setDataType(Const.META_TYPE_CLOB);
                    } else if ("blob".equalsIgnoreCase(datatype)) {
                        content.setDataType(Const.META_TYPE_BLOB);
                    }
                }
            }

        } catch (Exception ex) {
            throw new DAOException(ex);
        }
        return content;
    }


    private static void parsePrimaryKey(FieldContent fieldContent, Type type) {
        List<FieldContent> pkList = new ArrayList<>();
        if (!type.getClass().isPrimitive() && ((Class) type).getSuperclass().getCanonicalName().endsWith("BasePrimaryObject")) {
            Field[] fields = ((Class) type).getDeclaredFields();
            for (Field field : fields) {
                MappingField mapfield = field.getAnnotation(MappingField.class);
                if (mapfield != null) {
                    pkList.add(retrieveField(field, (Class<? extends BaseObject>) type));
                }
            }

        }
        if (!pkList.isEmpty()) {
            fieldContent.setPrimaryKeys(pkList);
        }
    }

    public static int replacementPrepared(PreparedStatement ps, LobHandler lobHandler, AnnotationRetrevior.FieldContent field, BaseObject object, int pos) throws SQLException {
        int tmppos = pos;
        Object value = getvalueFromVO(field, object);
        if (!field.isIncrement() && !field.isSequential() && value != null) {
            if (!field.isPrimary()) {
                wrapValue(ps, lobHandler, field, object, pos);
                tmppos++;
            } else {
                BasePrimaryObject compositeObj = (BasePrimaryObject) getvalueFromVO(field, object);
                List<AnnotationRetrevior.FieldContent> pkList = field.getPrimaryKeys();
                if (pkList != null) {
                    for (AnnotationRetrevior.FieldContent pks : pkList) {
                        if (!pks.isIncrement() && !pks.isSequential()) {
                            wrapValue(ps, lobHandler, pks, compositeObj, tmppos);
                            tmppos++;
                        }
                    }
                } else {
                    wrapValue(ps, lobHandler, field, object, pos);
                    tmppos++;
                }
            }
        }
        return tmppos;
    }

    public static Object getvalueFromVO(AnnotationRetrevior.FieldContent content, BaseObject object) {
        try {
            return content.getGetMethod().invoke(object, null);
        } catch (Exception ex) {

        }
        return null;
    }
    public static Map<String,Object> fieldContentToMap(FieldContent fieldContent){
        Map<String,Object> retMap=new HashMap<>();
        retMap.put("field",fieldContent.getFieldName());
        retMap.put("datatype",fieldContent.getDataType());
        retMap.put("precise",String.valueOf(fieldContent.getPrecise()));
        retMap.put("scale",String.valueOf(fieldContent.getScale()));
        retMap.put("length",String.valueOf(fieldContent.getLength()));
        return retMap;
    }

    private static void wrapValue(PreparedStatement ps, LobHandler lobHandler, AnnotationRetrevior.FieldContent field, BaseObject object, int pos) throws SQLException {
        Object value = getvalueFromVO(field, object);
        String datatype = field.getDataType();
        if (datatype.equalsIgnoreCase(Const.META_TYPE_CLOB)) {
            lobHandler.getLobCreator().setClobAsString(ps, pos, value.toString());
        } else if (datatype.equalsIgnoreCase(Const.META_TYPE_BLOB)) {
            lobHandler.getLobCreator().setBlobAsBytes(ps, pos, (byte[]) value);
        } else {
            setParameter(ps, pos, value);
        }

    }

    public static void setParameter(PreparedStatement stmt, int pos, Object obj) {
        try {
            if (obj == null) {
                if (pos != 0) {
                    stmt.setNull(pos, Types.VARCHAR);
                }
            } else if (obj instanceof Integer) {
                stmt.setInt(pos, Integer.parseInt(obj.toString()));
            } else if (obj instanceof Double) {
                stmt.setDouble(pos, Double.valueOf(obj.toString()));
            } else if (obj instanceof Date) {
                stmt.setDate(pos, (Date) obj);
            } else if (obj instanceof java.sql.Date) {
                stmt.setDate(pos, new Date(((java.sql.Date) obj).getTime()));
            } else if (obj instanceof Timestamp) {
                stmt.setTimestamp(pos, (Timestamp) obj);
            } else if (obj instanceof String) {
                stmt.setString(pos, obj.toString());
            } else if (obj instanceof Long) {
                stmt.setLong(pos, Long.parseLong(obj.toString()));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public static class EntityContent {
        boolean jpaAnnotation;
        private String tableName;
        private String schema;
        private String jdbcDao;

        public EntityContent(String tableName) {
            this.tableName = tableName;
        }

        public EntityContent(String tableName, boolean jpaAnnotation) {
            this.tableName = tableName;
            this.jpaAnnotation = jpaAnnotation;
        }

        public EntityContent(String tableName, String schema) {
            this.tableName = tableName;
            this.schema = schema;
        }

        public EntityContent(String tableName, String schema, boolean jpaAnnotation) {
            this.tableName = tableName;
            this.schema = schema;
            this.jpaAnnotation = jpaAnnotation;
        }

        public boolean isJpaAnnotation() {
            return jpaAnnotation;
        }

        public void setJpaAnnotation(boolean jpaAnnotation) {
            this.jpaAnnotation = jpaAnnotation;
        }

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public String getSchema() {
            return schema;
        }

        public void setSchema(String schema) {
            this.schema = schema;
        }

        public String getJdbcDao() {
            return jdbcDao;
        }

        public void setJdbcDao(String jdbcDao) {
            this.jdbcDao = jdbcDao;
        }
    }

    public static class FieldContent {
        private String propertyName;
        private String fieldName;
        private String dataType;
        private String sequenceName;
        private boolean required;
        private boolean increment;
        private boolean sequential;
        private boolean primary;
        private Field field;
        private Method getMethod;
        private Method setMethod;
        private int scale;
        private int precise;
        private int length;
        private List<FieldContent> primaryKeys;

        public FieldContent(String propertyName, String fieldName, Field field, Method getMethod, Method setMethod) {
            this.propertyName = propertyName;
            this.fieldName = fieldName;
            this.field = field;
            this.getMethod = getMethod;
            this.setMethod = setMethod;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public void setPropertyName(String propertyName) {
            this.propertyName = propertyName;
        }

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getDataType() {
            return dataType;
        }

        public void setDataType(String dataType) {
            this.dataType = dataType;
        }

        public String getSequenceName() {
            return sequenceName;
        }

        public void setSequenceName(String sequenceName) {
            this.sequenceName = sequenceName;
        }

        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }

        public boolean isIncrement() {
            return increment;
        }

        public void setIncrement(boolean increment) {
            this.increment = increment;
        }

        public boolean isSequential() {
            return sequential;
        }

        public void setSequential(boolean sequential) {
            this.sequential = sequential;
        }

        public boolean isPrimary() {
            return primary;
        }

        public void setPrimary(boolean primary) {
            this.primary = primary;
        }

        public Field getField() {
            return field;
        }

        public void setField(Field field) {
            this.field = field;
        }

        public Method getGetMethod() {
            return getMethod;
        }

        public void setGetMethod(Method getMethod) {
            this.getMethod = getMethod;
        }

        public Method getSetMethod() {
            return setMethod;
        }

        public void setSetMethod(Method setMethod) {
            this.setMethod = setMethod;
        }

        public int getScale() {
            return scale;
        }

        public void setScale(int scale) {
            this.scale = scale;
        }

        public int getPrecise() {
            return precise;
        }

        public void setPrecise(int precise) {
            this.precise = precise;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public List<FieldContent> getPrimaryKeys() {
            return primaryKeys;
        }

        public void setPrimaryKeys(List<FieldContent> primaryKeys) {
            this.primaryKeys = primaryKeys;
        }
    }
}
