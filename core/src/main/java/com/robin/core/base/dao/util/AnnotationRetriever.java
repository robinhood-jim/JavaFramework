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

import com.baomidou.mybatisplus.annotation.*;
import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.exception.DAOException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.model.BasePrimaryObject;
import com.robin.core.base.reflect.ReflectUtils;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.StringUtils;
import lombok.Data;
import org.springframework.jdbc.support.lob.LobHandler;

import javax.persistence.*;
import java.lang.invoke.SerializedLambda;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.sql.Date;
import java.sql.*;
import java.util.*;

public class AnnotationRetriever {
    private static Map<Class<? extends BaseObject>, Map<String, String>> tableCfgMap = new HashMap<>();
    private static Map<Class<? extends BaseObject>, EntityContent> entityCfgMap = new HashMap<>();
    private static Map<Class<? extends BaseObject>, Map<String, FieldContent>> fieldCfgMap = new HashMap<>();
    private static Map<Class<? extends BaseObject>, List<FieldContent>> fieldListMap = new HashMap<>();
    private static Map<String, WeakReference<SerializedLambda>> functionMap=new HashMap<>();
    private AnnotationRetriever(){

    }

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
            MappingEntity entity = clazz.getAnnotation(MappingEntity.class);

            Field[] fieldArr = clazz.getDeclaredFields();
            List<Field> fields=new ArrayList<>();
            List<Class> fieldClasses=new ArrayList<>();
            for(int i=0;i<fieldArr.length;i++){
                fields.add(fieldArr[i]);
                fieldClasses.add(clazz);
            }
            if(clazz.getSuperclass().getSuperclass().equals(BaseObject.class)){
                Field[] parentFields = clazz.getSuperclass().getDeclaredFields();
                for(Field field:parentFields){
                    fields.add(field);
                    fieldClasses.add(clazz.getSuperclass());
                }
            }
            for (int i=0;i<fields.size();i++) {
                Field field = fields.get(i);
                MappingField mapfield = field.getAnnotation(MappingField.class);
                if (mapfield != null || !entity.explicit() && !Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {
                    FieldContent content=retrieveField(field, fieldClasses.get(i));
                    if(content!=null) {
                        list.add(content);
                    }

                }
            }
        } else {
            //annotation with JPA
            flag = clazz.isAnnotationPresent(Entity.class);
            if (flag) {
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    Column mapfield = field.getAnnotation(Column.class);
                    if (mapfield != null ||(!Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers()))) {
                        FieldContent content=retrieveFieldJpa(field, clazz);
                        if(!Objects.isNull(content)) {
                            list.add(content);
                        }
                    }
                }
            }else{
                //annotation with MyBatisPlus
                flag=clazz.isAnnotationPresent(TableName.class);
                if(flag){
                    Field[] fields = clazz.getDeclaredFields();
                    for (Field field : fields) {
                        TableField mapfield = field.getAnnotation(TableField.class);
                        if (!Objects.isNull(mapfield) ||(!Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers()))) {
                            FieldContent content=retrieveFieldByMyBatis(field, clazz);
                            if(!Objects.isNull(content)) {
                                list.add(content);
                            }
                        }
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
            MappingEntity entity = clazz.getAnnotation(MappingEntity.class);
            for (Field field : fields) {
                MappingField mapfield = field.getAnnotation(MappingField.class);
                if (!Objects.isNull(mapfield) || !entity.explicit() && !Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {
                    FieldContent content=retrieveField(field, clazz);
                    if(content!=null) {
                        map.put(field.getName(), content);
                    }
                }
            }
        } else {
            flag = clazz.isAnnotationPresent(Entity.class);
            if (flag) {
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    Column mapfield = field.getAnnotation(Column.class);
                    if (!Objects.isNull(mapfield) ||(!Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers()))) {
                        map.put(field.getName(), retrieveFieldJpa(field, clazz));
                    }
                }
            }else{
                flag=clazz.isAnnotationPresent(TableName.class);
                if(flag){
                    Field[] fields=clazz.getDeclaredFields();
                    for(Field field:fields){
                        TableId tableIdField=field.getAnnotation(TableId.class);
                        if(tableIdField!=null && !Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {
                            FieldContent content=retrieveFieldByMyBatis(field,clazz);
                            if(!Objects.isNull(content)) {
                                map.put(field.getName(),content);
                            }
                        }
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
            content = getEntityContent(tableName, schema,false, false);
            if (!jdbcDao.isEmpty()) {
                content.setJdbcDao(jdbcDao);
            }
        } else {
            flag = clazz.isAnnotationPresent(Entity.class);
            if (flag) {
                Table table = clazz.getAnnotation(Table.class);
                String tableName = table.name();
                String schema = table.schema();
                content = getEntityContent(tableName, schema, true,false);
            } else {
                flag=clazz.isAnnotationPresent(TableName.class);
                if(flag){
                    TableName table=clazz.getAnnotation(TableName.class);
                    String tableName=table.value();
                    if(Objects.isNull(tableName)){
                        tableName=StringUtils.returnCamelCaseByFieldName(clazz.getName());
                    }
                    content =getEntityContent(tableName, table.schema(), false,true);
                }else {
                    throw new DAOException("must using MappingEntity or JPA or MybatisPlus annotation");
                }
            }
        }
        return content;
    }

    private static EntityContent getEntityContent(String tableName, String schema, boolean jpaAnnotation,boolean mybatisAnnotaion) {
        return new EntityContent(tableName,schema,jpaAnnotation,mybatisAnnotaion);
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
        //check model must using Annotation MappingEntity or Jpa Entity
        if (!ReflectUtils.isAnnotationClassWithAnnotationFields(object.getClass(), MappingEntity.class, MappingField.class) && !ReflectUtils.isAnnotationClassWithAnnotationFields(object.getClass(), Entity.class, Column.class) && !ReflectUtils.isAnnotationClassWithAnnotationFields(object.getClass(),TableName.class,TableId.class)) {
            throw new DAOException("Model object " + object.getClass().getSimpleName() + " must using Annotation MappingEntity or Jpa or MybatisPlus Entity");
        }
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

    public static boolean isBaseObjectClassValid(Class<? extends BaseObject> clazz) throws DAOException {
        if (!ReflectUtils.isAnnotationClassWithAnnotationFields(clazz, MappingEntity.class, MappingField.class)
                && !ReflectUtils.isAnnotationClassWithAnnotationFields(clazz, Entity.class, Column.class)
                && !ReflectUtils.isAnnotationClassWithAnnotationFields(clazz,TableName.class,TableId.class)) {
            throw new DAOException("Model object " + clazz.getSimpleName() + " must using Annotation MappingEntity or Jpa Entity or mybatis-plus Entity");
        }
        return true;
    }
    private static FieldContent retrieveFieldByMyBatis(Field field,Class<? extends BaseObject> clazz) throws DAOException{
        FieldContent content=null;
        try {
            String tmpName= org.springframework.util.StringUtils.capitalize(field.getName());
            Method getMethod = clazz.getMethod("get" + tmpName);
            Type type = getMethod.getReturnType();
            Method setMethod = clazz.getMethod("set" + tmpName,field.getType());
            TableField mapfield=field.getAnnotation(TableField.class);
            TableId idfield=field.getAnnotation(TableId.class);
            String fieldName;
            if(!Objects.isNull(mapfield)){
                if(!mapfield.exist()){
                    return null;
                }
                fieldName=mapfield.value();
            }else{
                if(!Objects.isNull(idfield)){
                    if(!StringUtils.isEmpty(idfield.value())){
                        fieldName=idfield.value();
                    }else{
                        fieldName = StringUtils.getFieldNameByCamelCase(field.getName());
                    }
                }else {
                    fieldName = StringUtils.getFieldNameByCamelCase(field.getName());
                }
            }
            content = new FieldContent(field.getName(), fieldName, field, getMethod, setMethod);
            if(!Objects.isNull(idfield)){
                content.setPrimary(true);
                if(!Objects.isNull(idfield.type())){
                    if(IdType.AUTO.equals(idfield.type())){
                        content.setIncrement(true);
                    }else if(IdType.INPUT.equals(idfield.type())){
                        KeySequence sequence= clazz.getAnnotation(KeySequence.class);
                        if(!Objects.isNull(sequence)){
                            content.setSequential(true);
                            content.setSequenceName(sequence.value());
                        }
                    }
                }
            }
            adjustByType(content,type);
        }catch (Exception ex){
            return null;
        }
        return content;
    }

    private static FieldContent retrieveFieldJpa(Field field, Class<? extends BaseObject> clazz) throws DAOException {
        FieldContent content = null;
        try {

            String tmpName = org.springframework.util.StringUtils.capitalize(field.getName());
            Method getMethod = clazz.getMethod("get" + tmpName, null);
            Type type = getMethod.getReturnType();
            Method setMethod = clazz.getMethod("set" + tmpName, field.getType());

            Column mapfield = field.getAnnotation(Column.class);
            String fieldName;
            if (mapfield != null && mapfield.name() != null && !mapfield.name().isEmpty()) {
                fieldName = mapfield.name();
            } else {
                fieldName = StringUtils.getFieldNameByCamelCase(field.getName());
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
            adjustByType(content,type);
        } catch (Exception ex) {
            throw new DAOException(ex);
        }
        return content;
    }

    private static FieldContent retrieveField(Field field, Class<? extends BaseObject> clazz) throws DAOException {
        FieldContent content = null;
        try {
            MappingEntity entity=clazz.getAnnotation(MappingEntity.class);
            MappingField mapfield = field.getAnnotation(MappingField.class);
            String name = field.getName();
            String colname = name.substring(0, 1).toUpperCase() + name.substring(1);

            Method getMethod = clazz.getDeclaredMethod("get" + colname, null);
            Type type = getMethod.getReturnType();
            Method setMethod = clazz.getDeclaredMethod("set" + colname, field.getType());
            String fieldName = null;
            String datatype = null;
            if (mapfield != null) {
                String colfield = mapfield.field();
                datatype = mapfield.datatype();

                if (colfield != null && !"".equals(colfield.trim())) {
                    fieldName = colfield;
                } else {
                    fieldName = StringUtils.getFieldNameByCamelCase(field.getName());
                }
            }else{
                fieldName = StringUtils.getFieldNameByCamelCase(field.getName());
            }
            content = new AnnotationRetriever.FieldContent(field.getName(), fieldName, field, getMethod, setMethod);
            if(mapfield!=null) {
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
                    parsePrimaryKey(content, type, entity.explicit());
                }
                if (mapfield.sequenceName() != null && !mapfield.sequenceName().isEmpty()) {
                    content.setSequential(true);
                    content.setSequenceName(mapfield.sequenceName());
                }
            }
            parseType(content,type,datatype);
        } catch (Exception ex) {
            return null;
        }
        return content;
    }



    private static void parsePrimaryKey(FieldContent fieldContent, Type type,boolean declareExplicit) {
        List<FieldContent> pkList = new ArrayList<>();
        if (!type.getClass().isPrimitive() && ((Class) type).getSuperclass().getCanonicalName().endsWith("BasePrimaryObject")) {
            Field[] fields = ((Class) type).getDeclaredFields();
            for (Field field : fields) {
                MappingField mapfield = field.getAnnotation(MappingField.class);
                if (mapfield != null || !declareExplicit) {
                    FieldContent content=retrieveField(field, (Class<? extends BaseObject>) type);
                    if(content!=null) {
                        pkList.add(content);
                    }
                }
            }

        }
        if (!pkList.isEmpty()) {
            fieldContent.setPrimaryKeys(pkList);
        }
    }

    public static int replacementPrepared(PreparedStatement ps, LobHandler lobHandler, AnnotationRetriever.FieldContent field, BaseObject object, int pos) throws SQLException {
        int tmppos = pos;
        Object value = getValueFromVO(field, object);
        if (!field.isIncrement() && !field.isSequential() && value != null) {
            if (!field.isPrimary()) {
                wrapValue(ps, lobHandler, field, object, pos);
                tmppos++;
            } else {
                BasePrimaryObject compositeObj = (BasePrimaryObject) getValueFromVO(field, object);
                List<AnnotationRetriever.FieldContent> pkList = field.getPrimaryKeys();
                if (pkList != null) {
                    for (AnnotationRetriever.FieldContent pks : pkList) {
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

    public static Object getValueFromVO(AnnotationRetriever.FieldContent content, BaseObject object) throws SQLException {
        try {
            return content.getGetMethod().invoke(object, null);
        }catch (Exception ex){
            throw new SQLException(ex);
        }
    }

    public static Map<String, Object> fieldContentToMap(FieldContent fieldContent) {
        Map<String, Object> retMap = new HashMap<>();
        retMap.put("field", fieldContent.getFieldName());
        retMap.put("datatype", fieldContent.getDataType());
        retMap.put("precise", String.valueOf(fieldContent.getPrecise()));
        retMap.put("scale", String.valueOf(fieldContent.getScale()));
        retMap.put("length", String.valueOf(fieldContent.getLength()));
        retMap.put("nullable", !fieldContent.isRequired());
        retMap.put("increment", fieldContent.isIncrement());
        return retMap;
    }
    public static <T> String getFieldName(PropertyFunction<T, ?> field) {
        Class<?> clazz = field.getClass();
        String name=clazz.getName();
        SerializedLambda lambda=Optional.ofNullable(functionMap.get(name)).map(Reference::get).orElseGet(()-> getLambdaSerialized(field));
        return uncapitalize(lambda.getImplMethodName());
    }
    private static <T> SerializedLambda getLambdaSerialized(PropertyFunction<T, ?> field){
        Class<?> clazz = field.getClass();
        String name=clazz.getName();
        try {
            Method method = clazz.getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            SerializedLambda lambda= (SerializedLambda) method.invoke(field);
            functionMap.put(name,new WeakReference<>(lambda));
            return lambda;
        } catch (Exception e) {
            if (!Object.class.isAssignableFrom(clazz.getSuperclass())) {
                return getLambdaSerialized(field);
            }
            throw new RuntimeException("current property is not exists");
        }
    }


    private static String uncapitalize(String str) {
        if (str == null || str.length() < 4) {
            return str;
        }
        String fieldName = str.startsWith("get") && !str.equalsIgnoreCase("getClass") ?
                str.substring(3) : str.startsWith("is") ? str.substring(2) : str;
        return fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
    }


    private static void wrapValue(PreparedStatement ps, LobHandler lobHandler, AnnotationRetriever.FieldContent field, BaseObject object, int pos) throws SQLException {
        Object value = getValueFromVO(field, object);
        if(value==null){
            setParameter(ps, pos, value);
            return;
        }
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
    private static void parseType(FieldContent content, Type type,String datatype){
        if (StringUtils.isEmpty(datatype)) {
            adjustByType(content,type);
        } else {
            if ("clob".equalsIgnoreCase(datatype)) {
                content.setDataType(Const.META_TYPE_CLOB);
            } else if ("blob".equalsIgnoreCase(datatype)) {
                content.setDataType(Const.META_TYPE_BLOB);
            }
        }
    }
    private static void adjustByType(FieldContent content,Type type){
        if (type.equals(Void.class)) {
            content.setDataType(Const.META_TYPE_INTEGER);
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
        }else if(type.equals(Short.class)){
            content.setDataType(Const.META_TYPE_SHORT);
        }
        else {
            content.setDataType(Const.META_TYPE_OBJECT);
        }
    }

    @Data
    public static class EntityContent {
        boolean jpaAnnotation;
        boolean mybatisAnnotation;
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
        public EntityContent(String tableName,String schema, boolean jpaAnnotation,boolean mybatisAnnotation) {
            this.tableName = tableName;
            this.jpaAnnotation = jpaAnnotation;
            this.mybatisAnnotation=mybatisAnnotation;
            this.schema=schema;
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

    }
    @Data
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

    }
}
