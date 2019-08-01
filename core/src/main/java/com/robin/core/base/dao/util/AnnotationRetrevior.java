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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.exception.DAOException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.util.Const;
import org.apache.commons.lang.math.NumberUtils;

public class AnnotationRetrevior {
    public static List<Map<String, Object>> getMappingFields(BaseObject obj, Map<String, String> tableMap, boolean needValidate) throws DAOException {
        boolean flag = obj.getClass().isAnnotationPresent(MappingEntity.class);
        if (flag) {
            MappingEntity entity = obj.getClass().getAnnotation(MappingEntity.class);
            String tableName = entity.table();
            String schema = entity.schema();
            tableMap.put("tableName", tableName);
            if (!"".equals(schema))
                tableMap.put("schema", schema);
            tableMap.put("jdbcDao", entity.jdbcDao());
            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            Field[] fields = obj.getClass().getDeclaredFields();

            for (Field field : fields) {
                MappingField mapfield = field.getAnnotation(MappingField.class);
                if (mapfield == null)
                    continue;
                Map<String, Object> map = retireveField(field, obj, needValidate);
                if (!map.isEmpty()) {
                    list.add(map);
                }
            }
            return list;
        } else {
            flag = obj.getClass().isAnnotationPresent(Entity.class);
            if (flag) {
                return getMappingFieldsByJpa(obj, tableMap, needValidate);
            } else
                throw new DAOException("must using MappingEnity annotation or Jpa");
        }
    }

    public static Map<String, String> getMappingTable(Class<?> clazz) throws DAOException {
        Map<String, String> tableMap = new HashMap<String, String>();
        boolean flag = clazz.isAnnotationPresent(MappingEntity.class);
        if (flag) {
            MappingEntity entity = clazz.getAnnotation(MappingEntity.class);
            String tableName = entity.table();
            String schema = entity.schema();
            tableMap.put("tableName", tableName);
            if (!"".equals(schema))
                tableMap.put("schema", schema);
            tableMap.put("jdbcDao", entity.jdbcDao());
            return tableMap;
        } else {
            flag=clazz.isAnnotationPresent(Entity.class);
            if(flag){
                Table table =clazz.getAnnotation(Table.class);
                tableMap.put("tableName", table.name());
                tableMap.put("schema",table.schema());
                return tableMap;
            }else
                throw new DAOException("must using MappingEnity or JPA annotation");
        }
    }

    public static List<Map<String, Object>> getMappingFieldsByJpa(BaseObject obj, Map<String, String> tableMap, boolean needValidate) throws DAOException {
        boolean flag = obj.getClass().isAnnotationPresent(Entity.class);
        if (flag) {
            Table table = obj.getClass().getAnnotation(Table.class);
            String tableName = table.name();
            String schema = table.schema();
            tableMap.put("tableName", tableName);
            if (!"".equals(schema))
                tableMap.put("schema", schema);
            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            Field[] fields = obj.getClass().getDeclaredFields();

            for (Field field : fields) {
                Map<String, Object> map = retireveFieldByJpa(field, obj, needValidate);
                if (!map.isEmpty()) {
                    list.add(map);
                }
            }
            return list;
        } else
            throw new DAOException("must using MappingEnity annotation");
    }

    public static Map<String, Object> getPrimaryField(List<Map<String, Object>> columList) {
        Map<String, Object> tmpMap = null;
        for (Map<String, Object> map : columList) {
            if (map.containsKey("primary")) {
                tmpMap = map;
                break;
            }
        }
        return tmpMap;
    }

    public static Map<String, Object> retireveField(Field field, BaseObject obj, final boolean needValidate) throws DAOException {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            MappingField mapfield = field.getAnnotation(MappingField.class);
            String name = field.getName();
            map.put("name", name);
            if(mapfield.precise()!=0)
                map.put("precise", mapfield.precise());
            if(mapfield.scale()!=0)
                map.put("scale", mapfield.scale());
            if(mapfield.length()!=0)
                map.put("length", mapfield.length());
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
            Method method = obj.getClass().getMethod("get" + name, null);
            Type type = method.getReturnType();
            Object value = method.invoke(obj, null);

            if (mapfield != null) {
                String colfield = mapfield.field();
                String datatype = mapfield.datatype();
                if (colfield != null && !"".equals(colfield.trim())) {
                    map.put("field", colfield);
                } else
                    map.put("field", name);
                map.put("value", value);
                boolean isincrement = mapfield.increment().equals("1");
                boolean isprimary = mapfield.primary().equals("1");
                boolean issequnce = !mapfield.sequenceName().equals("");
                if (isincrement)
                    map.put("increment", true);
                if (isprimary)
                    map.put("primary", true);
                if (issequnce) {
                    map.put("sequence", mapfield.sequenceName());
                }
                if (datatype == null || "".equals(datatype)) {
                    if (type.equals(Void.class)) {
                    } else if (type.equals(Long.class)) {
                        map.put("datatype", Const.META_TYPE_BIGINT);
                    } else if (type.equals(Integer.class)) {
                        map.put("datatype", Const.META_TYPE_INTEGER);
                    } else if (type.equals(Double.class)) {
                        map.put("datatype", Const.META_TYPE_NUMERIC);
                    } else if (type.equals(Float.class)) {
                        map.put("datatype", Const.META_TYPE_NUMERIC);
                    } else if (type.equals(String.class)) {
                        map.put("datatype", Const.META_TYPE_STRING);
                    } else if (type.equals(java.util.Date.class)) {
                        map.put("datatype", Const.META_TYPE_TIMESTAMP);
                    } else if (type.equals(Date.class)) {
                        map.put("datatype", Const.META_TYPE_DATE);
                    } else if (type.equals(byte[].class)) {
                        map.put("datatype", Const.META_TYPE_BLOB);
                    } else if (type.equals(Timestamp.class)) {
                        map.put("datatype", Const.META_TYPE_TIMESTAMP);
                    }
                } else {
                    if (datatype.equalsIgnoreCase("clob"))
                        map.put("datatype", Const.META_TYPE_CLOB);
                    else if (datatype.equalsIgnoreCase("blob")) {
                        map.put("datatype", Const.META_TYPE_BLOB);
                    }
                }
            }
            if (needValidate) {
                if (mapfield != null) {
                    boolean required = mapfield.required();
                    if (value == null && required && needValidate) {
                        throw new DAOException("column " + name + " must not be null!");
                    }
                    if(map.containsKey("scale") || map.containsKey("precise")){
                        if(value!=null && !NumberUtils.isNumber(value.toString())){
                            throw new DAOException("column " + name + " is not number!");
                        }
                    }
                    if(map.containsKey("length")){
                        if(value!=null && value.toString().length()>mapfield.length()){
                            throw new DAOException("column " + name + " is large than max length="+mapfield.length());
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new DAOException(ex);
        }
        return map;
    }

    public static Map<String, Object> retireveFieldByJpa(Field field, BaseObject obj, final boolean needValidate) throws DAOException {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            Id idfield = field.getAnnotation(Id.class);
            if (idfield != null) {
                map.put("primary", true);
                GeneratedValue genval = field.getAnnotation(GeneratedValue.class);
                if (genval != null) {
                    if (genval.strategy() == GenerationType.AUTO) {
                        map.put("increment", true);
                    } else if (genval.strategy() == GenerationType.IDENTITY) {
                        map.put("increment", true);
                    } else if (genval.strategy() == GenerationType.SEQUENCE) {
                        SequenceGenerator generator = field.getAnnotation(SequenceGenerator.class);
                        if (generator != null)
                            map.put("sequence", generator.sequenceName());
                    }
                }
            }
            Column mapfield = field.getAnnotation(Column.class);

            String name = field.getName();
            map.put("name", name);

            String tmname = name.substring(0, 1).toUpperCase() + name.substring(1, name.length());
            Method method = obj.getClass().getMethod("get" + tmname, null);
            Type type = method.getReturnType();
            Object value = method.invoke(obj, null);
            String property = "";
            if (mapfield != null) {
                property = name;
                String colfield = mapfield.name();
                if (colfield != null && !"".equals(colfield.trim())) {
                    map.put("field", colfield);
                } else
                    map.put("field", name);
            } else {
                map.put("field", name);
            }
            map.put("value", value);

            if (type.equals(Void.TYPE)) {
            } else if (type.equals(Long.TYPE)) {
                map.put("datatype", "int");
            } else if (type.equals(Integer.TYPE)) {
                map.put("datatype", "int");
            } else if (type.equals(Double.TYPE)) {
                map.put("datatype", "numeric");
            } else if (type.equals(Float.TYPE)) {
                map.put("datatype", "numeric");
            } else if (type.equals(String.class)) {
                map.put("datatype", "string");
            } else if (type.equals(java.util.Date.class)) {
                map.put("datatype", "date");
            } else if (type.equals(Date.class)) {
                map.put("datatype", "date");
            } else if (type.equals(byte[].class)) {
                map.put("datatype", "blob");
            } else if (type.equals(Timestamp.class)) {
                map.put("datatype", "timestamp");
            }


            if (needValidate) {
                if (mapfield != null) {
                    boolean required = !mapfield.nullable();
                    if (value == null && required && needValidate) {
                        throw new DAOException("column " + property + " must not be null!");
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new DAOException(ex);
        }
        return map;
    }
}
