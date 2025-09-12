/*
 * Copyright (c) 2015,robinjim
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
package com.robin.core.convert.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.robin.core.base.datameta.DataBaseColumnMeta;
import com.robin.core.base.exception.GenericException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.reflect.ReflectUtils;
import com.robin.core.base.service.IModelConvert;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Slf4j
public class ConvertUtil {
    public static final DateTimeFormatter ymdformatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static final DateTimeFormatter ymdSepformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter ymdSecondformatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    public static final DateTimeFormatter ymdSepSecondformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter ymdEupformatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static ThreadLocal<DateTimeFormatter> currentFormatter = new ThreadLocal<>();
    private static ThreadLocal<String> timeFormatStrLocal=new ThreadLocal<>();
    private static List<String> formatStrList=Lists.newArrayList("yyyyMMdd","yyyy-MM-dd","yyyyMMddHHmmss","yyyy-MM-dd HH:mm:ss","yyyy/MM/dd");
    private static List<DateTimeFormatter> formatterList=Lists.newArrayList(ymdformatter,ymdSepformatter,ymdSecondformatter,ymdSepSecondformatter,ymdEupformatter);


    private ConvertUtil() {

    }

    /**
     *
     * @param source
     * @param target
     * @param ignoreColumns
     * @throws Exception
     */
    public static void convertToTargetObj(Object source, Object target, String... ignoreColumns) throws Exception {
        if (target == null || source == null) {
            return;
        }

        Map<String, MethodHandle> srcmap = ReflectUtils.returnGetMethodHandle(source.getClass());
        Map<String, MethodHandle> targetMap = ReflectUtils.returnSetMethodHandle(target.getClass());
        List<String> ignoreColumnList = getIgnoreColumns(ignoreColumns);
        try {
            for (Map.Entry<String, MethodHandle> entry : srcmap.entrySet()) {
                if (targetMap.containsKey(entry.getKey()) && (ObjectUtils.isEmpty(ignoreColumnList) || !ignoreColumnList.contains(entry.getKey()))) {
                    Object value = parseParameter(targetMap.get(entry.getKey()).type().parameterType(1), srcmap.get(entry.getKey()).bindTo(source).invoke());
                    targetMap.get(entry.getKey()).bindTo(target).invoke(value);
                }
            }
        } catch (Throwable ex1) {
            throw new IllegalAccessException(ex1.getMessage());
        }
    }

    private static List<String> getIgnoreColumns(String[] ignoreColumns) {
        List<String> ignoreColumnList = null;
        if (ignoreColumns.length > 0) {
            ignoreColumnList = Lists.newArrayList(ignoreColumns);
        }
        return ignoreColumnList;
    }

    public static void objectToMap(Object source, Map<String, String> target) throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        if (source == null || target == null) {
            return;
        }
        Map<String, MethodHandle> getMethods = ReflectUtils.returnGetMethodHandle(source.getClass());
        try {
            for (Map.Entry<String, MethodHandle> entry : getMethods.entrySet()) {
                if (entry.getValue().type().parameterCount() == 1) {
                    Object value = entry.getValue().bindTo(source).invoke();
                    target.put(entry.getKey(), value == null ? "" : value.toString().trim());
                }
            }
        } catch (Throwable ex1) {
            throw new IllegalAccessException(ex1.getMessage());
        }
    }

    public static void objectToMapObj(Object source, Map<String, Object> target) throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        if (ObjectUtils.isEmpty(source)) {
            return;
        }
        Map<String, MethodHandle> getMetholds = ReflectUtils.returnGetMethodHandle(source.getClass());
        try {
            for (Map.Entry<String, MethodHandle> entry : getMetholds.entrySet()) {
                if (entry.getValue().type().parameterCount() == 1) {
                    Object value = entry.getValue().bindTo(source).invoke();
                    if (!ObjectUtils.isEmpty(value)) {
                        target.put(entry.getKey(), value);
                    }
                }
            }
        } catch (Throwable ex1) {
            throw new IllegalAccessException(ex1.getMessage());
        }
    }

    public static void objectToMapObj(Object source, Map<String, Object> target, String... ignoreColumns) throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        Set<String> ignoreSets = ignoreColumns.length > 0 ? Sets.newHashSet(ignoreColumns) : null;
        if (ObjectUtils.isEmpty(source)) {
            return;
        }
        Map<String, MethodHandle> getMetholds = ReflectUtils.returnGetMethodHandle(source.getClass());
        try {
            for (Map.Entry<String, MethodHandle> entry : getMetholds.entrySet()) {
                if (entry.getValue().type().parameterCount() == 1 && (CollectionUtils.isEmpty(ignoreSets) || !ignoreSets.contains(entry.getKey()))) {
                    Object value = entry.getValue().bindTo(target).invoke(source);
                    if (!ObjectUtils.isEmpty(value)) {
                        target.put(entry.getKey(), value);
                    }
                }
            }
        } catch (Throwable ex1) {
            throw new IllegalAccessException(ex1.getMessage());
        }
    }

    public static void mapToBaseObject(Map<String, String> source, BaseObject target, String... ignoreColumns) throws Exception {
        if (source == null || target == null) {
            return;
        }
        Iterator<Map.Entry<String, String>> it = source.entrySet().iterator();
        Map<String, MethodHandle> methodMap = ReflectUtils.returnSetMethodHandle(target.getClass());
        List<String> ignoreColumnList = getIgnoreColumns(ignoreColumns);
        try {
            while (it.hasNext()) {
                Map.Entry<String, String> entry = it.next();
                String key = entry.getKey();
                String value = entry.getValue();
                if (methodMap.containsKey(key) && (ObjectUtils.isEmpty(ignoreColumnList) || !ignoreColumnList.contains(key))) {
                    target.addDirtyColumn(key);
                    Class<?> type = methodMap.get(key).type().parameterType(1);
                    Object retValue;
                    if (StringUtils.isEmpty(value)) {
                        retValue = null;
                    } else {
                        retValue = parseParameter(type, value);
                    }
                    methodMap.get(key).bindTo(target).invoke(retValue);
                }
            }
        } catch (Throwable ex1) {
            throw new IllegalAccessException(ex1.getMessage());
        }

    }

    public static void mapToObject(Map<String, Object> source, Object target, String... ignoreColumns) throws Exception {
        if (source == null || target == null) {
            return;
        }
        Iterator<Map.Entry<String, Object>> it = source.entrySet().iterator();
        Map<String, MethodHandle> targetMap = ReflectUtils.returnSetMethodHandle(target.getClass());
        List<String> ignoreColumnList = getIgnoreColumns(ignoreColumns);
        try {
            while (it.hasNext()) {
                Map.Entry<String, Object> entry = it.next();
                String key = entry.getKey();
                Object value = entry.getValue();
                if (targetMap.containsKey(key) && (ObjectUtils.isEmpty(ignoreColumnList) || !ignoreColumnList.contains(key))) {
                    Class<?> type = targetMap.get(key).type().parameterType(1);
                    Object retValue;
                    if (ObjectUtils.isEmpty(value)) {
                        retValue = null;
                    } else {
                        retValue = parseParameter(type, value);
                    }
                    targetMap.get(key).bindTo(target).invoke(retValue);
                }
            }
        } catch (Throwable ex1) {
            throw new IllegalAccessException(ex1.getMessage());
        }
    }


    private static String camelCase(String value) {
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

    public static void convertSerializableForUpdate(Serializable source, Serializable target, String... ignoreColumns) throws Exception {
        if (target == null || source == null) {
            return;
        }
        if (!target.getClass().equals(source.getClass())) {
            throw new GenericException("source and target class mismatch");
        }
        Map<String, MethodHandle> srcmap = ReflectUtils.returnGetMethodHandle(source.getClass());
        Map<String, MethodHandle> targetMap = ReflectUtils.returnSetMethodHandle(target.getClass());
        List<String> ignoreColumnList = getIgnoreColumns(ignoreColumns);
        try {
            for (Map.Entry<String, MethodHandle> entry : srcmap.entrySet()) {
                String field = entry.getKey();
                if (ObjectUtils.isEmpty(ignoreColumnList) || !ignoreColumnList.contains(field)) {

                    Object value = entry.getValue().bindTo(source).invoke();
                    if (value != null && targetMap.get(entry.getKey()) != null) {
                        setObjectValue(target, value, targetMap.get(entry.getKey()));
                    }
                }
            }
        } catch (Throwable throwable) {
            throw new IllegalAccessException(throwable.getMessage());
        }
    }


    public static void convertToModelForUpdate(BaseObject source, BaseObject target) throws Exception {
        if (target == null || source == null) {
            return;
        }
        if (!target.getClass().equals(source.getClass())) {
            throw new RuntimeException("must have unique type");
        }
        Map<String, MethodHandle> srcmap = ReflectUtils.returnGetMethodHandle(source.getClass());
        Map<String, MethodHandle> targetmap = ReflectUtils.returnSetMethodHandle(source.getClass());
        try {
            if (!ObjectUtils.isEmpty(source.getDirtyColumn())) {
                for (String s : source.getDirtyColumn()) {
                    MethodHandle method = srcmap.get(s);
                    if (method != null) {
                        Object value = method.bindTo(source).invoke();
                        if (value != null) {
                            targetmap.get(s).bindTo(target).invoke(value);
                        } else {
                            targetmap.get(s).bindTo(target).invoke(null);
                        }
                    }
                }
            }
        } catch (Throwable ex1) {
            throw new IllegalAccessException(ex1.getMessage());
        }
    }

    public static void convertToModel(BaseObject target, Map<String, Object> src, String... ignoreColumns) throws Exception {
        if (target == null || src == null) {
            return;
        }
        Map<String, MethodHandle> map = ReflectUtils.returnSetMethodHandle(target.getClass());
        List<String> ignoreColumnList = getIgnoreColumns(ignoreColumns);
        try {
            for (Map.Entry<String, Object> entry : src.entrySet()) {
                String field = entry.getKey();
                if (ObjectUtils.isEmpty(ignoreColumnList) || !ignoreColumnList.contains(field)) {
                    MethodHandle setMethod = map.get(field);
                    if (setMethod != null) {
                        setBaseObjectValue(target, entry.getValue(), field, setMethod);
                    }
                }
            }
        } catch (Throwable ex1) {
            throw new IllegalAccessException(ex1.getMessage());
        }
    }

    private static void setBaseObjectValue(BaseObject target, Object value, String field, MethodHandle setMethod) throws Throwable {
        if (!ObjectUtils.isEmpty(value)) {
            target.addDirtyColumn(field);
            Class<?> type = setMethod.type().parameterType(1);
            Object retValue = parseParameter(type, value);
            setMethod.bindTo(target).invoke(retValue);
        } else if (target.getDirtyColumn().contains(field)) {
            setMethod.bindTo(target).invoke(null);
        }
    }

    private static void setObjectValue(Serializable target, Object value, MethodHandle setMethod) throws Throwable {
        if (!ObjectUtils.isEmpty(value)) {
            Class<?> type = setMethod.type().parameterType(1);
            Object retValue = parseParameter(type, value);
            if (retValue != null) {
                setMethod.bindTo(target).invoke(retValue);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void convertToTarget(Object source, Object target, String... ignoreColumns) throws Exception {
        if (target == null || source == null) {
            return;
        }

        List<String> ignoreColumnList = getIgnoreColumns(ignoreColumns);
        try {
            if (Map.class.isAssignableFrom(source.getClass())) {
                if(!Map.class.isAssignableFrom(target.getClass())) {
                    Map<String, Object> vMap = (Map<String, Object>) source;
                    Map<String, MethodHandle> targetMethodMap = ReflectUtils.returnSetMethodHandle(target.getClass());
                    for (Map.Entry<String, Object> entry : vMap.entrySet()) {
                        String field = entry.getKey();
                        MethodHandle setMethod = targetMethodMap.get(field);
                        if (setMethod != null) {
                            if (target instanceof BaseObject) {
                                setBaseObjectValue((BaseObject) target, entry.getValue(), field, setMethod);
                            } else {
                                if (targetMethodMap.containsKey(field) && canFill(ignoreColumnList,field)) {
                                    Object retValue = parseParameter(targetMethodMap.get(field).type().parameterType(1), entry.getValue());
                                    if (retValue != null) {
                                        setObjectValue(targetMethodMap.get(field), target, retValue);
                                    }
                                }
                            }
                        }
                    }
                }else{
                    ((Set<Map.Entry<?,?>>)((Map)source).entrySet()).forEach(entry->{
                        if(entry.getValue()!=null && canFill(ignoreColumnList,entry.getKey().toString())){
                            ((Map) target).put(entry.getKey(),entry.getValue());
                        }
                    });
                }
            } else {
                if(!Map.class.isAssignableFrom(target.getClass())) {
                    Map<String, MethodHandle> targetMethodMap = ReflectUtils.returnSetMethodHandle(target.getClass());
                    Map<String, MethodHandle> sourceMethodMap = ReflectUtils.returnGetMethodHandle(source.getClass());
                    for (Map.Entry<String, MethodHandle> entry : sourceMethodMap.entrySet()) {
                        if (targetMethodMap.containsKey(entry.getKey()) && entry.getValue().type().parameterCount() == 1) {
                            Object retValue = parseParameter(targetMethodMap.get(entry.getKey()).type().parameterType(1), entry.getValue().bindTo(source).invoke());
                            if (null != retValue) {
                                setObjectValue(targetMethodMap.get(entry.getKey()), target, retValue);
                            }
                        }
                    }
                }else{
                    Map<String, MethodHandle> sourceMethodMap = ReflectUtils.returnGetMethodHandle(source.getClass());
                    for (Map.Entry<String, MethodHandle> entry : sourceMethodMap.entrySet()) {
                        Object retValue=entry.getValue().bindTo(source).invoke();
                        if(retValue!=null && canFill(ignoreColumnList,entry.getKey())) {
                            ((Map) target).put(entry.getKey(), retValue);
                        }
                    }
                }
            }
        } catch (Throwable ex1) {
            throw new IllegalAccessException(ex1.getMessage());
        }
    }
    private static boolean canFill(List<String> ignoreColumnList,String field){
        return ObjectUtils.isEmpty(ignoreColumnList) || !ignoreColumnList.contains(field);
    }

    private static void setObjectValue(MethodHandle setMethod, Object target, Object value) throws Throwable {
        setMethod.bindTo(target).invoke(value);
    }

    public static Object parseParameter(DataBaseColumnMeta meta, Object strValue) {
        if (strValue == null) {
            return null;
        }
        DateTimeFormatter formatter = null;
        Object ret = null;
        if (!StringUtils.isEmpty(strValue)) {
            if (Types.INTEGER == meta.getDataType()) {
                ret = Integer.parseInt(strValue.toString());
            } else if (Types.BIGINT == meta.getDataType()) {
                ret = Long.parseLong(strValue.toString());
            } else if (Types.FLOAT == meta.getDataType()) {
                ret = Float.parseFloat(strValue.toString());
            } else if (Types.DOUBLE == meta.getDataType() || Types.DECIMAL == meta.getDataType()) {
                ret = Double.parseDouble(strValue.toString());
            } else if (Types.SMALLINT == meta.getDataType()) {
                ret = Short.valueOf(strValue.toString());
            } else if (Types.TIME == meta.getDataType() || Types.DATE == meta.getDataType() || Types.TIMESTAMP == meta.getDataType()) {
                String value = strValue.toString().trim();
                formatter = getFormatter(value);
                if (null != formatter) {
                    LocalDateTime localDateTime = LocalDateTime.parse(value, formatter);
                    if (Types.DATE == meta.getDataType()) {
                        ret = new java.util.Date(localDateTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli());
                    } else if (Types.TIMESTAMP == meta.getDataType()) {
                        ret = new Timestamp(localDateTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli());
                    }
                }

            } else if (Types.VARCHAR == meta.getDataType() || Types.CHAR == meta.getDataType()) {
                ret = strValue.toString();
            } else {
                ret = strValue.toString();
            }
        }
        return ret;
    }

    public static Object parseParameter(DataSetColumnMeta meta, Object strValue) {
        if (strValue == null) {
            return null;
        }
        DateTimeFormatter formatter = null;
        Object ret = null;
        if (!StringUtils.isEmpty(strValue)) {
            if (Const.META_TYPE_INTEGER.equals(meta.getColumnType())) {
                ret = Integer.parseInt(strValue.toString());
            } else if (Const.META_TYPE_BIGINT.equals(meta.getColumnType())) {
                ret = Long.parseLong(strValue.toString());
            } else if (Const.META_TYPE_FLOAT.equals(meta.getColumnType())) {
                ret = Float.parseFloat(strValue.toString());
            } else if (Const.META_TYPE_DOUBLE.equals(meta.getColumnType()) || Const.META_TYPE_DECIMAL.equals(meta.getColumnType())) {
                ret = Double.parseDouble(strValue.toString());
            } else if (Const.META_TYPE_SHORT.equals(meta.getColumnType())) {
                ret = Short.valueOf(strValue.toString());
            } else if (Const.META_TYPE_DATE.equals(meta.getColumnType()) || Const.META_TYPE_TIMESTAMP.equals(meta.getColumnType())) {
                String value = strValue.toString().trim();
                formatter = getFormatter(value);
                if (null != formatter) {
                    LocalDateTime localDateTime = LocalDateTime.parse(value, formatter);
                    if (Const.META_TYPE_DATE.equals(meta.getColumnType())) {
                        ret = new java.util.Date(localDateTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli());
                    } else if (Const.META_TYPE_TIMESTAMP.equals(meta.getColumnType())) {
                        ret = new Timestamp(localDateTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli());
                    }
                }

            } else if (Const.META_TYPE_STRING.equals(meta.getColumnType())) {
                ret = strValue.toString();
            } else {
                ret = strValue.toString();
            }
        }
        return ret;
    }


    public static Object parseParameter(Class<?> type, Object strValue) throws Exception {
        if (Objects.isNull(strValue)) {
            return null;
        }
        DateTimeFormatter formatter = null;
        Object ret = null;
        if (!ObjectUtils.isEmpty(strValue)) {
            if (Integer.class.isAssignableFrom(type)) {
                ret = Integer.parseInt(strValue.toString());
            } else if (Long.class.isAssignableFrom(type)) {
                ret = Long.parseLong(strValue.toString());
            } else if (Float.class.isAssignableFrom(type)) {
                ret = Float.parseFloat(strValue.toString());
            } else if (Double.class.isAssignableFrom(type)) {
                ret = Double.parseDouble(strValue.toString());
            } else if (Short.class.isAssignableFrom(type)) {
                ret = Short.valueOf(strValue.toString());
            } else if (BigDecimal.class.isAssignableFrom(type)) {
                ret = BigDecimal.valueOf(Double.parseDouble(strValue.toString()));
            } else if (Boolean.class.isAssignableFrom(type)) {
                if (NumberUtils.isNumber(strValue.toString())) {
                    ret = strValue.toString().equals(Const.VALID);
                } else {
                    ret = Boolean.valueOf(strValue.toString());
                }
            } else if (type.isAssignableFrom(java.util.Date.class) || type.isAssignableFrom(LocalDateTime.class) || type.isAssignableFrom(Timestamp.class)) {

                if (type.isAssignableFrom(java.util.Date.class)) {
                    if (java.util.Date.class.isAssignableFrom(strValue.getClass())) {
                        ret = strValue;
                    } else {
                        LocalDateTime localDateTime = getLocalDateTimeFrom(strValue.toString());
                        ret = new java.util.Date(localDateTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli());
                    }
                } else if (type.isAssignableFrom(Timestamp.class)) {
                    if (Timestamp.class.isAssignableFrom(strValue.getClass())) {
                        ret = strValue;
                    } else {
                        LocalDateTime localDateTime = getLocalDateTimeFrom(strValue.toString());
                        ret = new Timestamp(localDateTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli());
                    }
                } else if (type.isAssignableFrom(LocalDateTime.class)) {
                    if (LocalDateTime.class.isAssignableFrom(strValue.getClass())) {
                        ret = strValue;
                    } else {
                        ret = getLocalDateTimeFrom(strValue);
                    }
                }
            } else if (type.isAssignableFrom(String.class)) {
                ret = strValue.toString();
            } else {

                if (type.isAssignableFrom(byte.class)) {
                    if (!ObjectUtils.isEmpty(strValue)) {
                        Method method = type.getMethod("valueOf", String.class);
                        if (method != null) {
                            ret = method.invoke(Class.forName("java.lang.Byte"), strValue);
                        }
                    }
                } else if (type.isAssignableFrom(byte[].class)) {
                    ret = strValue;
                } else {
                    ret = strValue;
                }
            }
        }
        return ret;
    }

    private static LocalDateTime getLocalDateTimeFrom(Object strValue) {
        LocalDateTime ret=null;
        DateTimeFormatter formatter;
        formatter = !ObjectUtils.isEmpty(currentFormatter.get()) ? currentFormatter.get() : getFormatter(strValue.toString());
        if(formatter!=null) {
            if (ymdformatter.equals(formatter) || ymdEupformatter.equals(formatter) || ymdEupformatter.equals(formatter)) {
                ret = LocalDate.parse(strValue.toString(), formatter).atStartOfDay();
            } else {
                ret = LocalDateTime.parse(strValue.toString(), formatter);
            }
        }else if(NumberUtils.isNumber(strValue.toString())){
            ret=LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.valueOf(strValue.toString())),ZoneId.systemDefault());
        }
        return ret;
    }

    public static DateTimeFormatter getFormatter(String value) {
        DateTimeFormatter retFormatter = currentFormatter.get();
        if (ObjectUtils.isEmpty(retFormatter)) {
            if (isFormatterFit(value, ymdformatter)) {
                retFormatter = ymdformatter;
            } else if (isFormatterFit(value, ymdSepformatter)) {
                retFormatter = ymdSepformatter;
            } else if (isFormatterFit(value, ymdSecondformatter)) {
                retFormatter = ymdSecondformatter;
            } else if (isFormatterFit(value, ymdSepSecondformatter)) {
                retFormatter = ymdSepSecondformatter;
            } else if (isFormatterFit(value, ymdEupformatter)) {
                retFormatter = ymdEupformatter;
            }
            if(retFormatter!=null) {
                currentFormatter.set(retFormatter);
            }
        }
        return retFormatter;
    }

    private static boolean isFormatterFit(String value, DateTimeFormatter formatter) {
        try {
            formatter.parse(value);
            return true;
        } catch (Exception ex) {

        }
        return false;
    }

    public static Object convertStringToTargetObject(String value, DataSetColumnMeta meta, DateTimeFormatter formatter) {
        Object retObj;
        String columnType = meta.getColumnType();
        retObj = translateValue(value, columnType, meta.getColumnName(), formatter);
        if (retObj == null && meta.getDefaultNullValue() != null) {
            retObj = meta.getDefaultNullValue();
        }
        return retObj;
    }


    public static Object wrapObjectByAutoDetect(Object object, String dateFormatStr) {
        Object retObj = null;
        if (object != null) {
            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(dateFormatStr != null && !dateFormatStr.isEmpty() ? dateFormatStr : "yyyy-MM-dd");
            Class<?> clazz = object.getClass();
            if (object.getClass().isPrimitive()) {
                if (clazz.equals(String.class)) {
                    if (NumberUtils.isNumber(object.toString())) {
                        Number number = NumberUtils.createNumber(object.toString());
                        if (Math.ceil(number.doubleValue()) == number.longValue()) {
                            if (number.longValue() < Integer.MAX_VALUE) {
                                retObj = number.intValue();
                            } else {
                                retObj = number.longValue();
                            }
                        } else {
                            retObj = number.doubleValue();
                        }
                    } else {
                        try {
                            LocalDateTime date = LocalDateTime.parse(object.toString(), dateFormat);
                            retObj = Timestamp.valueOf(date);
                        } catch (DateTimeParseException ex) {
                            retObj = object;
                        }
                    }
                } else {
                    retObj = object;
                }
            } else {
                retObj = object;
            }
        }
        return retObj;
    }

    private static Object translateValue(String value, String columnType, String columnName, DateTimeFormatter dateformat) throws RuntimeException {
        Object retObj;
        try {
            if (value == null || StringUtils.isEmpty(value.trim())) {
                return null;
            }
            if (columnType.equals(Const.META_TYPE_INTEGER)) {
                if (value.contains(".")) {
                    value = value.substring(0, value.indexOf("."));
                }
                retObj = Integer.valueOf(value);
            } else if (columnType.equals(Const.META_TYPE_BIGINT)) {
                if (value.contains(".")) {
                    value = value.substring(0, value.indexOf("."));
                }
                retObj = Long.valueOf(value);
            } else if (columnType.equals(Const.META_TYPE_NUMERIC)) {
                retObj = Double.valueOf(value);
            } else if (columnType.equals(Const.META_TYPE_DOUBLE)) {
                retObj = Double.valueOf(value);
            } else if (columnType.equals(Const.META_TYPE_DATE)) {
                retObj = new java.util.Date(LocalDateTime.parse(value, dateformat).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            } else if (columnType.equals(Const.META_TYPE_TIMESTAMP)) {
                retObj = new Timestamp(LocalDateTime.parse(value, dateformat).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            } else {
                retObj = value;
            }
        } catch (Exception ex) {
            throw new RuntimeException("columnName =" + columnName + ",type=" + columnType + " with value=" + value + "failed! type mismatch,Please check!");
        }
        return retObj;
    }

    public static <T> T wrapVoObject(Object obj, IModelConvert<T> convert, Class<T> voType, String... ignoreKeys) {
        T voObj;
        if (convert != null) {
            voObj = convert.doConvert(obj);
        } else {
            if (obj.getClass().getSuperclass().getInterfaces().length > 0 && Map.class.isAssignableFrom(obj.getClass())) {
                voObj = (T) ConvertUtil.sourceToTargetWithMap((Map) obj, voType, ignoreKeys);
            } else {
                voObj = BeanUtils.instantiateClass(voType);
                BeanUtils.copyProperties(obj, voObj, ignoreKeys);
            }
        }
        return voObj;
    }

    public static <T> T sourceToTargetWithMap(Map<String, Object> map, Class<T> target, String... ignoreColumns) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        Set<String> ignoreKeys = new HashSet<>();
        boolean hasIgnore = false;
        if (ignoreColumns.length > 0) {
            ignoreKeys.addAll(Arrays.asList(ignoreColumns));
            hasIgnore = true;
        }
        T targetObject = null;
        try {
            Map<String, MethodHandle> methodMap = ReflectUtils.returnSetMethodHandle(target);
            targetObject = target.newInstance();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (entry.getValue() != null) {
                    if (ObjectUtils.isEmpty(entry.getValue())) {
                        continue;
                    }
                    if (methodMap.containsKey(entry.getKey()) && (!hasIgnore || !ignoreKeys.contains(entry.getKey()))) {
                        setFields(targetObject, methodMap.get(entry.getKey()), entry.getValue());
                    }
                }
            }

        } catch (Exception e) {
            log.error("convert error {}", e);
        }
        return targetObject;
    }


    private static <T> void setFields(T target, MethodHandle method, Object value) {
        try {
            if (value != null) {
                method.bindTo(target).invoke(parseParameter(method.type().parameterType(1), value));
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    public static <T> void sourceToMap(Map<String, Object> map, T sourceObj, String defaultTimeFormat, String... ignoreKeys) {
        try {
            Map<String, MethodHandle> methodMap = ReflectUtils.returnGetMethodHandle(sourceObj.getClass());
            Iterator<Map.Entry<String, MethodHandle>> iterator = methodMap.entrySet().iterator();
            List<String> ignoreList = new ArrayList<>();
            DateTimeFormatter timeFormatter = !ObjectUtils.isEmpty(defaultTimeFormat) ? DateTimeFormatter.ofPattern(defaultTimeFormat) : ymdSecondformatter;
            if (ignoreKeys != null && ignoreKeys.length > 0) {
                for (int i = 0; i < ignoreKeys.length; i++) {
                    ignoreList.addAll(Arrays.asList(ignoreKeys[i]));
                }
            }
            while (iterator.hasNext()) {
                Map.Entry<String, MethodHandle> entry = iterator.next();
                Object value = entry.getValue().bindTo(sourceObj).invoke();
                if (!ignoreList.contains(entry.getKey()) && value != null) {
                    if (value.getClass().isAssignableFrom(LocalDateTime.class)) {
                        map.put(entry.getKey(), timeFormatter.format((LocalDateTime) value));
                    } else {
                        map.put(entry.getKey(), value);
                    }
                }

            }
        } catch (Throwable ex) {
            log.error("{}", ex);
        }
    }

    public static void setDateTimeFormat(String formatStr) {
        int pos=formatStrList.indexOf(formatStr);
        if(pos!=-1){
            currentFormatter.set(formatterList.get(pos));
        }else {
            currentFormatter.set(DateTimeFormatter.ofPattern(formatStr));
        }
        timeFormatStrLocal.set(formatStr);
    }

    public static void setDateTimeFormat(DateTimeFormatter formatter) {
        int pos=formatStrList.indexOf(formatter.toString());
        if(pos!=-1){
            currentFormatter.set(formatterList.get(pos));
        }else {
            currentFormatter.set(formatter);
        }
        timeFormatStrLocal.set(formatter.toString());
    }

    public static void finishConvert() {
        if (currentFormatter.get() != null) {
            currentFormatter.remove();
        }
    }

    public static DateTimeFormatter getCurrentFormat() {
        if (currentFormatter.get() != null) {
            return currentFormatter.get();
        }
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }
}
