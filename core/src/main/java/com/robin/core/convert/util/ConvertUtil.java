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
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
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

    private ConvertUtil() {

    }

    public static void convertToTargetObj(Object target, Object src, String... defaultDateTimeFormatter) throws Exception {
        if (target == null || src == null) {
            return;
        }

        Map<String, Method> srcmap = ReflectUtils.returnGetMethods(src.getClass());
        Map<String, Method> targetMap = ReflectUtils.returnSetMethods(target.getClass());

        for (Map.Entry<String, Method> entry : srcmap.entrySet()) {
            if (targetMap.containsKey(entry.getKey())) {
                Object value = parseParameter(targetMap.get(entry.getKey()).getParameterTypes()[0], srcmap.get(entry.getKey()).invoke(src, (Object[]) null), defaultDateTimeFormatter);
                targetMap.get(entry.getKey()).invoke(target, value);
            }
        }
    }

    public static void objectToMap(Map<String, String> target, Object src) throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        if (src == null || target == null) {
            return;
        }
        Map<String, Method> getMethods = ReflectUtils.returnGetMethods(src.getClass());
        for (Map.Entry<String, Method> entry : getMethods.entrySet()) {
            if (entry.getValue().getParameterTypes().length == 0) {
                Object value = entry.getValue().invoke(src);
                target.put(entry.getKey(), value == null ? "" : value.toString().trim());
            }
        }
    }

    public static void objectToMapObj(Map<String, Object> target, Object src) throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        if (ObjectUtils.isEmpty(src)) {
            return;
        }
        Map<String, Method> getMetholds = ReflectUtils.returnGetMethods(src.getClass());
        for (Map.Entry<String, Method> entry : getMetholds.entrySet()) {
            if (entry.getValue().getParameterTypes().length == 0) {
                Object value = entry.getValue().invoke(src);
                target.put(entry.getKey(), value);
            }
        }
    }

    public static void mapToObject(BaseObject target, Map<String, String> src, String... defaultDateTimeFormatter) throws Exception {
        if (src == null || target == null) {
            return;
        }
        Iterator<Map.Entry<String, String>> it = src.entrySet().iterator();
        Map<String, Method> methodMap = ReflectUtils.returnSetMethods(target.getClass());
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            String key = entry.getKey();
            String value = entry.getValue();
            if (methodMap.containsKey(key)) {
                target.addDirtyColumn(key);
                Class<?> type = methodMap.get(key).getParameterTypes()[0];
                Object retValue;
                if (StringUtils.isEmpty(value)) {
                    retValue = null;
                } else {
                    retValue = parseParameter(type, value, defaultDateTimeFormatter);
                }
                methodMap.get(key).invoke(target, retValue);
            }
        }

    }

    public static void mapToObject(Object target, Map<String, Object> src, String... defaultDateTimeFormatter) throws Exception {
        if (src == null || target == null) {
            return;
        }

        Iterator<Map.Entry<String, Object>> it = src.entrySet().iterator();
        Map<String, Method> targetMap = ReflectUtils.returnSetMethods(target.getClass());
        while (it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();
            String key = entry.getKey();
            Object value = entry.getValue();
            if (targetMap.containsKey(key)) {
                Class<?> type = targetMap.get(key).getParameterTypes()[0];
                Object retValue;
                if (ObjectUtils.isEmpty(value)) {
                    retValue = null;
                } else {
                    retValue = parseParameter(type, value, defaultDateTimeFormatter);
                }
                targetMap.get(key).invoke(target, retValue);
            }
        }
    }


    private static String wordCase(String value) {
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

    public static void convertSerializableForUpdate(Serializable target, Serializable src, String... defaultDateTime) throws Exception {
        if (target == null || src == null) {
            return;
        }
        if (!target.getClass().equals(src.getClass())) {
            throw new GenericException("source and target class mismatch");
        }
        Map<String, Method> srcmap = ReflectUtils.returnGetMethods(src.getClass());
        for (Map.Entry<String, Method> entry : srcmap.entrySet()) {
            String field = entry.getKey();
            Method setMethod = entry.getValue();
            Object value = entry.getValue().invoke(null);
            if (value != null && setMethod != null) {
                setObjectValue(target, value, field, setMethod, defaultDateTime);
            }
        }
    }


    public static void convertToModelForUpdate(BaseObject target, BaseObject src) throws Exception {
        if (target == null || src == null) {
            return;
        }
        if (!target.getClass().equals(src.getClass())) {
            throw new RuntimeException("");
        }
        Map<String, Method> srcmap = ReflectUtils.returnGetMethods(src.getClass());
        Map<String, Method> targetMap = ReflectUtils.returnSetMethods(target.getClass());

        List<String> dirtyColumnList = src.getDirtyColumn();
        for (String s : dirtyColumnList) {
            Method setMethod = targetMap.get(s);
            if (setMethod != null) {
                Method getMethod = srcmap.get(s);
                Object value = getMethod.invoke(src, (Object[]) null);
                if (value != null) {
                    setMethod.invoke(target, value);
                } else {
                    setMethod.invoke(target);
                }
            }
        }
    }

    public static void convertToModel(BaseObject target, Map<String, String> src, String... defaultDateTimeFormatter) throws Exception {
        if (target == null || src == null) {
            return;
        }

        Map<String, Method> map = ReflectUtils.returnSetMethods(target.getClass());

        for (Map.Entry<String, String> entry : src.entrySet()) {
            String field = entry.getKey();
            Method setMethod = map.get(field);
            if (setMethod != null) {
                setBaseObjectValue(target, entry.getValue(), field, setMethod, defaultDateTimeFormatter);
            }
        }
    }

    private static void setBaseObjectValue(BaseObject target, Object value, String field, Method setMethod, String... defaultDateTimeFormatter) throws Exception {
        if (!ObjectUtils.isEmpty(value)) {
            target.addDirtyColumn(field);
            Class<?> type = setMethod.getParameterTypes()[0];
            Object retValue = parseParameter(type, value, defaultDateTimeFormatter);
            setMethod.invoke(target, retValue);
        }else if(target.getDirtyColumn().contains(field)){
            setMethod.invoke(target);
        }
    }

    private static void setObjectValue(Serializable target, Object value, String field, Method setMethod, String... defaultDateTimeFormatter) throws Exception {
        if (value != null) {
            Class<?> type = setMethod.getParameterTypes()[0];
            if (!"java.lang.String".equalsIgnoreCase(type.getName()) && "".equals(value)) {
                setMethod.invoke(target);
            } else {
                Object retValue = parseParameter(type, value, defaultDateTimeFormatter);
                if(retValue!=null) {
                    setMethod.invoke(target, retValue);
                }
            }
        }
    }
    @SuppressWarnings("unchecked")
    public static void convertToTarget(Object target, Object src, String... defaultDateTimeFormatter) throws Exception {
        if (target == null || src == null) {
            return;
        }
        Map<String, Method> targetMethodMap = ReflectUtils.returnSetMethods(target.getClass());
        Map<String, Method> sourceMethodMap = ReflectUtils.returnGetMethods(src.getClass());
        if (Map.class.isAssignableFrom(src.getClass())) {
            Map<String, Object> vMap = (Map<String, Object>) src;
            for (Map.Entry<String, Object> entry : vMap.entrySet()) {
                String field = entry.getKey();
                Method setMethod = targetMethodMap.get(field);
                if (setMethod != null) {
                    if (target instanceof BaseObject) {
                        setBaseObjectValue((BaseObject) target, entry.getValue(), field, setMethod);
                    } else {
                        if (targetMethodMap.containsKey(field)) {
                            Object retValue = parseParameter(targetMethodMap.get(field).getParameterTypes()[0], entry.getValue(), defaultDateTimeFormatter);
                            if(retValue!=null) {
                                setObjectValue(targetMethodMap.get(field), target, retValue);
                            }
                        }
                    }
                }
            }
        } else {
            for (Map.Entry<String, Method> entry : sourceMethodMap.entrySet()) {
                if (targetMethodMap.containsKey(entry.getKey()) && entry.getValue().getParameterTypes().length == 0) {
                    Object retValue = parseParameter(targetMethodMap.get(entry.getKey()).getParameterTypes()[0], entry.getValue().invoke(src), defaultDateTimeFormatter);
                    if (null != retValue) {
                        setObjectValue(targetMethodMap.get(entry.getKey()), target, retValue);
                    }
                }
            }
        }
    }

    private static void setObjectValue(Method setMethod, Object target, Object value) throws Exception {
        setMethod.invoke(target, value);
    }

    public static Object parseParameter(DataBaseColumnMeta meta, Object strValue, String... defaultDateTimeFormatter) {
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
                if (defaultDateTimeFormatter.length == 0) {
                    formatter = getFormatter(value);
                } else {
                    formatter = DateTimeFormatter.ofPattern(defaultDateTimeFormatter[0]);
                }
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
    public static Object parseParameter(DataSetColumnMeta meta, Object strValue, String... defaultDateTimeFormatter) {
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
                if (defaultDateTimeFormatter.length == 0) {
                    formatter = getFormatter(value);
                } else {
                    formatter = DateTimeFormatter.ofPattern(defaultDateTimeFormatter[0]);
                }
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


    public static Object parseParameter(Class<?> type, Object strValue, String... defaultDateTimeFormatter) throws Exception {
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
                        formatter = getFormatter(defaultDateTimeFormatter, strValue.toString());
                        LocalDateTime localDateTime = LocalDateTime.parse(strValue.toString(), formatter);
                        ret = new java.util.Date(localDateTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli());
                    }
                } else if (type.isAssignableFrom(Timestamp.class)) {
                    if (Timestamp.class.isAssignableFrom(strValue.getClass())) {
                        ret = strValue;
                    } else {
                        formatter = getFormatter(defaultDateTimeFormatter, strValue.toString());
                        LocalDateTime localDateTime = LocalDateTime.parse(strValue.toString(), formatter);
                        ret = new Timestamp(localDateTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli());
                    }
                } else if (type.isAssignableFrom(LocalDateTime.class)) {
                    if (LocalDateTime.class.isAssignableFrom(strValue.getClass())) {
                        ret = strValue;
                    } else {
                        formatter = getFormatter(defaultDateTimeFormatter, strValue.toString());
                        ret = LocalDateTime.parse(strValue.toString(), formatter);
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

    private static DateTimeFormatter getFormatter(String[] defaultDateTimeFormatter, String value) {
        DateTimeFormatter formatter;
        if (defaultDateTimeFormatter.length == 0) {
            formatter = getFormatter(value);
        } else {
            formatter = DateTimeFormatter.ofPattern(defaultDateTimeFormatter[0]);
        }
        return formatter;
    }


    public static DateTimeFormatter getFormatter(String value) {
        DateTimeFormatter retFormatter = null;
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

    public static Object convertStringToTargetObject(String value, DataSetColumnMeta meta){
        Object retObj;
        DateTimeFormatter dateformat ;
        String dateformatstr = meta.getDateFormat();
        if (dateformatstr == null || StringUtils.isEmpty(dateformatstr)) {
            dateformatstr = Const.DEFAULT_DATETIME_FORMAT;
        }
        dateformat = DateTimeFormatter.ofPattern(dateformatstr);
        String columnType = meta.getColumnType();
        retObj = translateValue(value, columnType, meta.getColumnName(), dateformat);
        if (retObj == null && meta.getDefaultNullValue() != null) {
            retObj = meta.getDefaultNullValue();
        }
        return retObj;
    }

    public static Object convertStringToTargetObject(String value, String columnType, String columnName, String defaultDateTimefromat) {
        Object retObj;
        DateTimeFormatter dateformat = null;
        String dateformatstr = defaultDateTimefromat;
        if (dateformatstr == null || StringUtils.isEmpty(dateformatstr)) {
            dateformatstr = Const.DEFAULT_DATETIME_FORMAT;
        }
        dateformat = DateTimeFormatter.ofPattern(dateformatstr);
        retObj = translateValue(value, columnType, columnName, dateformat);
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
                            LocalDateTime date = LocalDateTime.parse(object.toString(),dateFormat);
                            retObj =Timestamp.valueOf(date);
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
                retObj = Integer.valueOf(value);
            } else if (columnType.equals(Const.META_TYPE_BIGINT)) {
                retObj = Long.valueOf(value);
            } else if (columnType.equals(Const.META_TYPE_NUMERIC)) {
                retObj = Double.valueOf(value);
            } else if (columnType.equals(Const.META_TYPE_DOUBLE)) {
                retObj = Double.valueOf(value);
            } else if (columnType.equals(Const.META_TYPE_DATE)) {
                retObj = new java.util.Date(LocalDateTime.parse(value,dateformat).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            } else if (columnType.equals(Const.META_TYPE_TIMESTAMP)) {
                retObj = new Timestamp(LocalDateTime.parse(value,dateformat).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
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
            if(obj.getClass().getSuperclass().getInterfaces().length>0 &&  Map.class.isAssignableFrom(obj.getClass())){
                voObj=(T)ConvertUtil.sourceToTargetWithMap((Map)obj,voType,ignoreKeys);
            }else {
                voObj = BeanUtils.instantiateClass(voType);
                BeanUtils.copyProperties(obj, voObj,ignoreKeys);
            }
        }
        return voObj;
    }
    public static <T> T sourceToTargetWithMap(Map<String, Object> map, Class<T> target,String... ignoreColumns) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        Set<String> ignoreKeys=new HashSet<>();
        boolean hasIgnore=false;
        if(ignoreColumns.length>0){
            ignoreKeys.addAll(Arrays.asList(ignoreColumns));
            hasIgnore=true;
        }
        T targetObject = null;
        Map<String, Method> methodMap = ReflectUtils.returnSetMethods(target);
        try {
            targetObject = target.newInstance();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (entry.getValue() != null) {
                    if (entry.getValue() instanceof String) {
                        if (StringUtils.isEmpty(entry.getValue().toString())) {
                            continue;
                        }
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


    private static <T> void setFields(T target, Method method, Object value) {
        try {
            if (value != null) {
                method.invoke(target, parseParameter(method.getParameterTypes()[0], value));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public static <T> void sourceToMap(Map<String, Object> map, T sourceObj,String defaultTimeFormat, String... ignoreKeys) {
        Map<String, Method> methodMap = ReflectUtils.returnGetMethods(sourceObj.getClass());
        Iterator<Map.Entry<String, Method>> iterator = methodMap.entrySet().iterator();
        List<String> ignoreList = new ArrayList<>();
        DateTimeFormatter timeFormatter=!ObjectUtils.isEmpty(defaultTimeFormat)?DateTimeFormatter.ofPattern(defaultTimeFormat):ymdSecondformatter;
        if (ignoreKeys != null && ignoreKeys.length > 0) {
            for (int i = 0; i < ignoreKeys.length; i++) {
                ignoreList.addAll(Arrays.asList(ignoreKeys[i]));
            }
        }
        while (iterator.hasNext()) {
            try {
                Map.Entry<String, Method> entry = iterator.next();
                Object value = entry.getValue().invoke(sourceObj);
                if (!ignoreList.contains(entry.getKey()) && value != null) {
                    if(value.getClass().isAssignableFrom(LocalDateTime.class)){
                        map.put(entry.getKey(),timeFormatter.format((LocalDateTime) value));
                    }else {
                        map.put(entry.getKey(), value);
                    }
                }
            } catch (Exception ex) {
                log.error("{}", ex);
            }
        }
    }
}
