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
import com.robin.core.base.exception.ConfigurationIncorrectException;
import com.robin.core.base.exception.DAOException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.model.BasePrimaryObject;
import com.robin.core.base.reflect.ReflectUtils;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.StringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.util.ObjectUtils;

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

@Slf4j
@SuppressWarnings("unchecked")
public class AnnotationRetriever {
    private static final Map<Class<? extends BaseObject>, EntityContent> entityCfgMap = new HashMap<>();
    private static final Map<Class<? extends BaseObject>, Map<String, FieldContent>> fieldCfgMap = new HashMap<>();
    private static final Map<Class<? extends BaseObject>, List<FieldContent>> fieldListMap = new HashMap<>();
    private static final Map<String, WeakReference<SerializedLambda>> functionMap = new HashMap<>();
    private static final Map<String, WeakReference<Class<? extends BaseObject>>> annotationClassMap = new HashMap<>();

    private AnnotationRetriever() {

    }

    public static List<FieldContent> getMappingFieldsCache(Class<? extends BaseObject> clazz) throws DAOException {
        List<FieldContent> list;
        if (!fieldListMap.containsKey(clazz)) {
            list = getMappingFields(clazz);
            fieldListMap.put(clazz, list);
        } else {
            list = fieldListMap.get(clazz);
        }
        return list;
    }

    public static Map<String, FieldContent> getMappingFieldsMapCache(Class<? extends BaseObject> clazz) throws DAOException {
        Map<String, FieldContent> map;
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
            List<Field> fields = new ArrayList<>();
            List<Class<? extends BaseObject>> fieldClasses = new ArrayList<>();
            for (Field value : fieldArr) {
                fields.add(value);
                fieldClasses.add(clazz);
            }
            if (clazz.getSuperclass().getSuperclass().equals(BaseObject.class)) {
                Field[] parentFields = clazz.getSuperclass().getDeclaredFields();
                for (Field field : parentFields) {
                    fields.add(field);
                    fieldClasses.add((Class<? extends BaseObject>)clazz.getSuperclass());
                }
            }
            for (int i = 0; i < fields.size(); i++) {
                Field field = fields.get(i);
                MappingField mapfield = field.getAnnotation(MappingField.class);
                if (isFieldValid(entity, mapfield, field)) {
                    FieldContent content = retrieveField(field, fieldClasses.get(i));
                    if (content != null) {
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
                    if (mapfield != null || (!Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers()))) {
                        FieldContent content = retrieveFieldJpa(field, clazz);
                        if (!Objects.isNull(content)) {
                            list.add(content);
                        }
                    }
                }
            } else {
                //annotation with MyBatisPlus
                flag = clazz.isAnnotationPresent(TableName.class);
                if (flag) {
                    Field[] fields = clazz.getDeclaredFields();
                    for (Field field : fields) {
                        TableField mapfield = field.getAnnotation(TableField.class);
                        if ((!Objects.isNull(mapfield) && mapfield.exist()) || (!Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers()))) {
                            FieldContent content = retrieveFieldByMyBatis(field, clazz);
                            if (!Objects.isNull(content)) {
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
                if (isFieldValid(entity, mapfield, field)) {
                    FieldContent content = retrieveField(field, clazz);
                    if (content != null) {
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
                    if (!Objects.isNull(mapfield) || (!Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers()))) {
                        map.put(field.getName(), retrieveFieldJpa(field, clazz));
                    }
                }
            } else {
                flag = clazz.isAnnotationPresent(TableName.class);
                if (flag) {
                    Field[] fields = clazz.getDeclaredFields();
                    for (Field field : fields) {
                        TableId tableIdField = field.getAnnotation(TableId.class);
                        if (tableIdField != null && !Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {
                            FieldContent content = retrieveFieldByMyBatis(field, clazz);
                            if (!Objects.isNull(content)) {
                                map.put(field.getName(), content);
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
        if (!entityCfgMap.containsKey(clazz)) {
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
            String tableName = entity.value();
            String schema = entity.schema();
            String jdbcDao = entity.jdbcDao();
            content = getEntityContent(tableName, schema, false, false);
            if (!ObjectUtils.isEmpty(jdbcDao)) {
                content.setJdbcDao(jdbcDao);
            }
        } else {
            flag = clazz.isAnnotationPresent(Entity.class);
            if (flag) {
                Entity entity = clazz.getAnnotation(Entity.class);
                Table table = clazz.getAnnotation(Table.class);
                String tableName;
                String schema = null;
                if (!ObjectUtils.isEmpty(table)) {
                    tableName = table.name();
                    schema = table.schema();
                } else {
                    tableName = entity.name();
                }
                content = getEntityContent(tableName, schema, true, false);
            } else {
                flag = clazz.isAnnotationPresent(TableName.class);
                if (flag) {
                    TableName table = clazz.getAnnotation(TableName.class);
                    String tableName = table.value();
                    if (Objects.isNull(tableName)) {
                        tableName = StringUtils.returnCamelCaseByFieldName(clazz.getName());
                    }
                    content = getEntityContent(tableName, table.schema(), false, true);
                } else {
                    throw new DAOException("must using MappingEntity or JPA or MybatisPlus annotation");
                }
            }
        }
        return content;
    }

    private static EntityContent getEntityContent(String tableName, String schema, boolean jpaAnnotation, boolean mybatisAnnotaion) {
        return new EntityContent(tableName, schema, jpaAnnotation, mybatisAnnotaion);
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

    public static FieldContent getPrimaryFieldByClass(Class<? extends BaseObject> clazz) {
        List<FieldContent> fields = AnnotationRetriever.getMappingFieldsCache(clazz);
        return getPrimaryField(fields);
    }

    public static void validateEntity(BaseObject object) throws DAOException {
        //check model must using Annotation MappingEntity or Jpa Entity
        if (!ReflectUtils.isAnnotationClassWithAnnotationFields(object.getClass(), MappingEntity.class, MappingField.class) && !ReflectUtils.isAnnotationClassWithAnnotationFields(object.getClass(), Entity.class, Column.class) && !ReflectUtils.isAnnotationClassWithAnnotationFields(object.getClass(), TableName.class, TableId.class)) {
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
                && !ReflectUtils.isAnnotationClassWithAnnotationFields(clazz, TableName.class, TableId.class)) {
            throw new DAOException("Model object " + clazz.getSimpleName() + " must using Annotation MappingEntity or Jpa Entity or mybatis-plus Entity");
        }
        return true;
    }

    private static FieldContent retrieveFieldByMyBatis(Field field, Class<? extends BaseObject> clazz) throws DAOException {
        FieldContent content;
        try {
            String tmpName = org.springframework.util.StringUtils.capitalize(field.getName());
            Method getMethod = clazz.getMethod("get" + tmpName);
            Type type = getMethod.getReturnType();
            Method setMethod = clazz.getMethod("set" + tmpName, field.getType());
            TableField mapfield = field.getAnnotation(TableField.class);
            TableId idfield = field.getAnnotation(TableId.class);
            String fieldName;
            FieldContent.Builder builder = new FieldContent.Builder();
            builder.setGetMethod(getMethod).setSetMethod(setMethod).setField(field).setPropertyName(field.getName());
            if (!Objects.isNull(mapfield)) {
                if (!mapfield.exist()) {
                    return null;
                }
                if (!ObjectUtils.isEmpty(mapfield.update())) {
                    builder.setDefaultValue(mapfield.update());
                }
                if (!ObjectUtils.isEmpty(mapfield.numericScale())) {
                    builder.setScale(Integer.parseInt(mapfield.numericScale()));
                }
                if (!ObjectUtils.isEmpty(mapfield.value())) {
                    fieldName = mapfield.value();
                } else {
                    fieldName = StringUtils.getFieldNameByCamelCase(field.getName());
                }
            } else {
                if (!Objects.isNull(idfield) && !StringUtils.isEmpty(idfield.value())) {
                    fieldName = idfield.value();
                } else {
                    fieldName = StringUtils.getFieldNameByCamelCase(field.getName());
                }
            }
            builder.setFieldName(fieldName);

            if (!Objects.isNull(idfield)) {
                builder.setPrimary(true);
                if (!Objects.isNull(idfield.type())) {
                    if (IdType.AUTO.equals(idfield.type())) {
                        builder.setIncrement(true);
                    } else if (IdType.INPUT.equals(idfield.type())) {
                        KeySequence sequence = clazz.getAnnotation(KeySequence.class);
                        if (!Objects.isNull(sequence)) {
                            builder.setSequential(true).setSequenceName(sequence.value());
                        }
                    }
                }
            }
            content = builder.build();
            adjustByType(content, type);

        } catch (Exception ex) {
            return null;
        }
        return content;
    }

    private static FieldContent retrieveFieldJpa(Field field, Class<? extends BaseObject> clazz) throws DAOException {
        FieldContent content ;
        try {

            String tmpName = org.springframework.util.StringUtils.capitalize(field.getName());
            Method getMethod = clazz.getMethod("get" + tmpName);
            Type type = getMethod.getReturnType();
            Method setMethod = clazz.getMethod("set" + tmpName, field.getType());

            Column mapfield = field.getAnnotation(Column.class);
            Id idfield = field.getAnnotation(Id.class);
            String fieldName = null;
            FieldContent.Builder builder = new FieldContent.Builder();
            builder.setGetMethod(getMethod).setSetMethod(setMethod).setField(field).setPropertyName(field.getName());
            if (mapfield != null) {
                if (mapfield.name() != null && !mapfield.name().isEmpty()) {
                    fieldName = mapfield.name();
                }
                if (mapfield.length() != 0) {
                    builder.setLength(mapfield.length());
                }
                if (mapfield.scale() != 0) {
                    builder.setScale(mapfield.scale());
                }
                if (mapfield.precision() != 0) {
                    builder.setPrecise(mapfield.precision());
                }
            }
            if (ObjectUtils.isEmpty(fieldName)) {
                fieldName = StringUtils.getFieldNameByCamelCase(field.getName());
            }
            builder.setFieldName(fieldName);
            if (idfield != null) {
                builder.setPrimary(true);
                GeneratedValue genval = field.getAnnotation(GeneratedValue.class);
                if (genval != null) {
                    if (genval.strategy() == GenerationType.IDENTITY) {
                        builder.setIncrement(true);
                    } else if (genval.strategy() == GenerationType.SEQUENCE) {
                        SequenceGenerator generator = field.getAnnotation(SequenceGenerator.class);
                        if (generator != null) {
                            builder.setSequential(true).setSequenceName(generator.sequenceName());
                        }
                    }
                }
            }
            content = builder.build();
            adjustByType(content, type);

        } catch (Exception ex) {
            throw new DAOException(ex);
        }
        return content;
    }

    private static FieldContent retrieveField(Field field, Class<? extends BaseObject> clazz) throws DAOException {
        FieldContent content;
        try {
            MappingEntity entity = clazz.getAnnotation(MappingEntity.class);
            MappingField mapfield = field.getAnnotation(MappingField.class);
            String name = field.getName();
            String colname = name.substring(0, 1).toUpperCase() + name.substring(1);
            Method getMethod = clazz.getDeclaredMethod("get" + colname);
            Type type = getMethod.getReturnType();
            Method setMethod = clazz.getDeclaredMethod("set" + colname, field.getType());
            String fieldName = null;
            String datatype = null;
            FieldContent.Builder builder = new FieldContent.Builder();
            builder.setGetMethod(getMethod).setSetMethod(setMethod).setField(field).setPropertyName(field.getName());
            if (mapfield != null) {
                String colfield = mapfield.value();
                datatype = mapfield.datatype();
                if (!ObjectUtils.isEmpty(colfield)) {
                    fieldName = colfield;
                }
                if (mapfield.precise() != 0) {
                    builder.setPrecise(mapfield.precise());
                }
                if (mapfield.scale() != 0) {
                    builder.setScale(mapfield.scale());
                }
                if (mapfield.length() != 0) {
                    builder.setLength(mapfield.length());
                }
                if (mapfield.increment()) {
                    builder.setIncrement(true);
                }
                if (mapfield.primary()) {
                    builder.setPrimary(true);
                }
                if (!ObjectUtils.isEmpty(mapfield.sequenceName())) {
                    builder.setSequential(true).setSequenceName(mapfield.sequenceName());
                }
            }
            if (ObjectUtils.isEmpty(fieldName)) {
                fieldName = StringUtils.getFieldNameByCamelCase(field.getName());
            }
            builder.setFieldName(fieldName);
            content = builder.build();
            if (content.isPrimary()) {
                parsePrimaryKey(content, type, entity.explicit());
            }
            parseType(content, type, datatype);
        } catch (Exception ex) {
            return null;
        }
        return content;
    }


    private static void parsePrimaryKey(FieldContent fieldContent, Type type, boolean declareExplicit) {
        List<FieldContent> pkList = new ArrayList<>();
        if (!type.getClass().isPrimitive() && ((Class) type).getSuperclass().getCanonicalName().endsWith("BasePrimaryObject")) {
            Field[] fields = ((Class) type).getDeclaredFields();
            for (Field field : fields) {
                MappingField mapfield = field.getAnnotation(MappingField.class);
                if (mapfield != null || !declareExplicit) {
                    FieldContent content = retrieveField(field, (Class<? extends BaseObject>) type);
                    if (content != null) {
                        pkList.add(content);
                    }
                }
            }

        }
        if (!pkList.isEmpty()) {
            fieldContent.setPrimaryKeys(pkList);
        }
    }

    public static int replacementPrepared(PreparedStatement ps, LobHandler lobHandler, FieldContent field, BaseObject object, int pos, EntityMappingUtil.InsertSegment insertSegment) throws SQLException {
        int tmppos = pos;
        Object value = getValueFromVO(field, object);
        if (!field.isIncrement() && !field.isSequential() && value != null) {
            if (!field.isPrimary()) {
                wrapValue(ps, lobHandler, field, object, pos);
                tmppos++;
            } else {
                BasePrimaryObject compositeObj = (BasePrimaryObject) getValueFromVO(field, object);
                List<FieldContent> pkList = field.getPrimaryKeys();
                if (pkList != null) {
                    for (FieldContent pks : pkList) {
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

    public static Object getValueFromVO(FieldContent content, BaseObject object) throws SQLException {
        try {
            return content.getGetMethod().invoke(object, null);
        } catch (Exception ex) {
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
        String name = clazz.getName();
        SerializedLambda lambda = Optional.ofNullable(functionMap.get(name)).map(Reference::get).orElseGet(() -> getLambdaSerialized(field));
        return uncapitalize(lambda.getImplMethodName());
    }
    public static <T extends BaseObject> String getFieldColumnName(PropertyFunction<T, ?> field) {
        Class<T> clazz = getFieldOwnedClass(field);
        String fieldName=getFieldName(field);
        Map<String, FieldContent> map = getMappingFieldsMapCache(clazz);
        return Optional.ofNullable(map.get(fieldName)).map(f->f.getFieldName()).orElse(fieldName);
    }

    public static <T extends BaseObject> Class<T> getFieldOwnedClass(PropertyFunction<T, ?> field) {
        Class<?> clazz = field.getClass();
        String name = clazz.getName();
        SerializedLambda lambda = Optional.ofNullable(functionMap.get(name)).map(Reference::get).orElseGet(() -> getLambdaSerialized(field));
        Class<T> aClass = null;
        if (ObjectUtils.isEmpty(annotationClassMap.get(lambda.getImplClass()))) {
            try {
                aClass = (Class<T>) Class.forName(lambda.getImplClass().replace("/", "."));
                annotationClassMap.put(lambda.getImplClass(), new WeakReference<>(aClass));
            } catch (ClassNotFoundException ex) {
                log.error("{}", ex.getMessage());
            }
        }else{
            aClass=(Class<T>)annotationClassMap.get(lambda.getImplClass()).get();
        }
        return aClass;
    }

    public static <T> String getFieldType(PropertyFunction<T, ?> field) {
        Class<?> clazz = field.getClass();
        String name = clazz.getName();
        SerializedLambda lambda = Optional.ofNullable(functionMap.get(name)).map(Reference::get).orElseGet(() -> getLambdaSerialized(field));
        String retClass = lambda.getImplMethodSignature();
        String retType = Const.META_TYPE_STRING;

        if ("()Ljava/lang/Long;".equals(retClass)) {
            retType = Const.META_TYPE_BIGINT;
        } else if ("()Ljava/lang/Double;".equals(retClass) || "()Ljava/lang/BigDecimal;".equals(retClass)) {
            retType = Const.META_TYPE_DOUBLE;
        } else if ("()Ljava/lang/Integer;".equals(retClass)) {
            retType = Const.META_TYPE_INTEGER;
        } else if ("()Ljava/lang/Short;".equals(retClass)) {
            retType = Const.META_TYPE_SHORT;
        } else if ("()Ljava/lang/byte;".equals(retClass)) {
            retType = Const.META_TYPE_BLOB;
        } else if ("()Ljava/sql/Date;".equals(retClass) || "()Ljava/util/Date;".equals(retClass) || "()Ljava/time/LocalDate;".equals(retClass)) {
            retType = Const.META_TYPE_DATE;
        } else if ("()Ljava/sql/Timestamp;".equals(retClass) || "()Ljava/time/LocalDateTime;".equals(retClass)) {
            retType = Const.META_TYPE_TIMESTAMP;
        }
        return retType;
    }
    public static <T extends BaseObject> String getFieldType(FieldContent content){
        Class<?> clazz=content.getField().getType();
        String retType = Const.META_TYPE_STRING;
        if(Long.class.isAssignableFrom(clazz)){
            retType=Const.META_TYPE_BIGINT;
        }else if(Double.class.isAssignableFrom(clazz) || Float.class.isAssignableFrom(clazz)){
            retType=Const.META_TYPE_DOUBLE;
        }else if(Integer.class.isAssignableFrom(clazz)){
            retType=Const.META_TYPE_INTEGER;
        }else if(Short.class.isAssignableFrom(clazz)){
            retType=Const.META_TYPE_SHORT;
        }else if(byte[].class.isAssignableFrom(clazz)){
            retType=Const.META_TYPE_BLOB;
        }else if(Date.class.isAssignableFrom(clazz)){
            retType=Const.META_TYPE_DATE;
        }else if(java.util.Date.class.isAssignableFrom(clazz) && Timestamp.class.isAssignableFrom(clazz)){
            retType=Const.META_TYPE_TIMESTAMP;
        }
        return retType;
    }

    private static <T> SerializedLambda getLambdaSerialized(PropertyFunction<T, ?> field) {
        Class<?> clazz = field.getClass();
        String name = clazz.getName();
        try {
            Method method = clazz.getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            SerializedLambda lambda = (SerializedLambda) method.invoke(field);
            functionMap.put(name, new WeakReference<>(lambda));
            return lambda;
        } catch (Exception e) {
            if (!Object.class.isAssignableFrom(clazz.getSuperclass())) {
                return getLambdaSerialized(field);
            }
            throw new ConfigurationIncorrectException("current property is not exists");
        }
    }


    private static String uncapitalize(String str) {
        if (str == null || str.length() < 4) {
            return str;
        }
        String fieldName = str.startsWith("get") && !"getClass".equalsIgnoreCase(str) ?
                str.substring(3) : str.startsWith("is") ? str.substring(2) : str;
        return fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
    }


    private static void wrapValue(PreparedStatement ps, LobHandler lobHandler, FieldContent field, BaseObject object, int pos) throws SQLException {
        Object value = getValueFromVO(field, object);
        if (value == null) {
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
            } else if (Integer.class.isAssignableFrom(obj.getClass())) {
                stmt.setInt(pos, Integer.parseInt(obj.toString()));
            } else if (Double.class.isAssignableFrom(obj.getClass())) {
                stmt.setDouble(pos, Double.valueOf(obj.toString()));
            } else if (Date.class.isAssignableFrom(obj.getClass())) {
                stmt.setDate(pos, (Date) obj);
            } else if (java.util.Date.class.isAssignableFrom(obj.getClass())) {
                stmt.setDate(pos, new Date(((java.util.Date) obj).getTime()));
            } else if (Timestamp.class.isAssignableFrom(obj.getClass())) {
                stmt.setTimestamp(pos, (Timestamp) obj);
            } else if (Long.class.isAssignableFrom(obj.getClass())) {
                stmt.setLong(pos, Long.parseLong(obj.toString()));
            } else {
                stmt.setString(pos, obj.toString());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void parseType(FieldContent content, Type type, String datatype) {
        if (StringUtils.isEmpty(datatype)) {
            adjustByType(content, type);
        } else {
            if ("clob".equalsIgnoreCase(datatype)) {
                content.setDataType(Const.META_TYPE_CLOB);
            } else if ("blob".equalsIgnoreCase(datatype)) {
                content.setDataType(Const.META_TYPE_BLOB);
            }
        }
    }

    private static boolean isFieldValid(MappingEntity entity, MappingField mapfield, Field field) {
        return ((mapfield != null && !mapfield.exclude()) || !entity.explicit())
                && !Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers());
    }

    private static void adjustByType(FieldContent content, Type type) {
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
        } else if (type.equals(Short.class)) {
            content.setDataType(Const.META_TYPE_SHORT);
        } else {
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

        public EntityContent(String tableName, String schema, boolean jpaAnnotation, boolean mybatisAnnotation) {
            this.tableName = tableName;
            this.jpaAnnotation = jpaAnnotation;
            this.mybatisAnnotation = mybatisAnnotation;
            this.schema = schema;
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

}
