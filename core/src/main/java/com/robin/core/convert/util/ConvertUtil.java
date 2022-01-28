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

import com.robin.core.base.model.BaseObject;
import com.robin.core.base.reflect.ReflectUtils;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;


import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ConvertUtil {
    public static final DateTimeFormatter ymdformatter=DateTimeFormatter.ofPattern("yyyyMMdd");
    public static final DateTimeFormatter ymdSepformatter=DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter ymdSecondformatter=DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    public static final DateTimeFormatter ymdSepSecondformatter=DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter ymdEupformatter=DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private ConvertUtil(){

    }

    public static void convertToTargetObj(Object target, Object src,String... defaultDateTimeFormatter) throws Exception {
        if (target == null || src == null) {
            return;
        }

        Map<String, Method> srcmap = ReflectUtils.returnGetMethods(src.getClass());
        Map<String,Method> targetMap= ReflectUtils.returnSetMethods(target.getClass());

        Iterator<Map.Entry<String,Method>> iterator=srcmap.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<String,Method> entry=iterator.next();
            if(targetMap.containsKey(entry.getKey())){
                Object value= parseParameter(targetMap.get(entry.getKey()).getParameterTypes()[0],srcmap.get(entry.getKey()).invoke(src,(Object[]) null),defaultDateTimeFormatter);
                targetMap.get(entry.getKey()).invoke(target,value);
            }
        }
    }

    public static void objectToMap(Map<String, String> target, Object src) throws Exception {
        if (src == null || target == null) {
            return;
        }
        Map<String,Method> getMethods= ReflectUtils.returnGetMethods(src.getClass());
        Iterator<Map.Entry<String,Method>> iterator=getMethods.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<String,Method> entry=iterator.next();
            if(entry.getValue().getParameterTypes().length==0) {
                Object value = entry.getValue().invoke(src, (Object[]) null);
                target.put(entry.getKey(), value == null ? "" : value.toString().trim());
            }
        }
    }

    public static void objectToMapObj(Map<String, Object> target, Object src) throws Exception {
        if (src == null || target == null) {
            return;
        }
        Map<String,Method> getMetholds= ReflectUtils.returnGetMethods(src.getClass());
        Iterator<Map.Entry<String,Method>> iterator=getMetholds.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<String,Method> entry=iterator.next();
            if(entry.getValue().getParameterTypes().length==0) {
                Object value = entry.getValue().invoke(src, (Object[]) null);
                target.put(entry.getKey(), value);
            }
        }
    }

    public static void mapToObject(BaseObject target, Map<String, String> src,String... defaultDateTimeFormatter) throws Exception {
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
                target.AddDirtyColumn(key);
                Class type = methodMap.get(key).getParameterTypes()[0];
                Object retValue = null;
                if (StringUtils.isEmpty(value)) {
                    retValue = null;
                } else {
                    retValue = parseParameter(type, value,defaultDateTimeFormatter);
                }
                methodMap.get(key).invoke(target,retValue);
            }
        }

    }

    public static void mapToObject(Object target, Map<String, Object> src,String... defaultDateTimeFormatter) throws Exception {
        if (src == null || target == null) {
            return;
        }

        Iterator<Map.Entry<String,Object>> it = src.entrySet().iterator();
        Map<String,Method> targetMap= ReflectUtils.returnSetMethods(target.getClass());
        while (it.hasNext()) {
            Map.Entry<String,Object> entry=it.next();
            String key = entry.getKey();
            Object value = entry.getValue();
            if (targetMap.containsKey(key)) {
                Class type = targetMap.get(key).getParameterTypes()[0];
                Object retValue = null;
                if (StringUtils.isEmpty(value)) {
                    retValue = null;
                } else {
                    retValue = parseParameter(type, value,defaultDateTimeFormatter);
                }
                targetMap.get(key).invoke(target, retValue);
            }
        }
    }


    private static String wordCase(String value) {
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

    public static void convertSerializableForUpdate(Serializable target, Serializable src,String... defaultDateTime) throws Exception {
        if (target == null || src == null) {
            return;
        }
        if (!target.getClass().equals(src.getClass())) {
            throw new RuntimeException("source and target class mismatch");
        }
        Map<String, Method> srcmap = ReflectUtils.returnGetMethods(src.getClass());
        Iterator<Map.Entry<String,Method>> set = srcmap.entrySet().iterator();
        while (set.hasNext()) {
            Map.Entry<String,Method> entry=set.next();
            String field = entry.getKey();
            Method setMethod = entry.getValue();
            Object value=entry.getValue().invoke(null);
            if (value!=null && setMethod != null) {
                setObjectValue(target,value,field,setMethod,defaultDateTime);
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
        Map<String,Method> targetMap= ReflectUtils.returnSetMethods(target.getClass());

        List<String> dirtyColumnList = src.getDirtyColumn();
        for (int i = 0; i < dirtyColumnList.size(); i++) {

            Method setMethod = targetMap.get(dirtyColumnList.get(i));
            if (setMethod != null) {
                Method getMethod = srcmap.get(dirtyColumnList.get(i));
                Object value = getMethod.invoke(src, (Object[]) null);
                if (value != null) {
                    setMethod.invoke(target, value);
                } else {
                    setMethod.invoke(target);
                }
            }

        }
    }

    public static void convertToModel(BaseObject target, Map<String, String> src,String... defaultDateTimeFormatter) throws Exception {
        if (target == null || src == null) {
            return;
        }

        Map<String, Method> map = ReflectUtils.returnSetMethods(target.getClass());

        Iterator<Map.Entry<String,String>> set = src.entrySet().iterator();
        while (set.hasNext()) {
            Map.Entry<String,String> entry=set.next();
            String field = entry.getKey();
            Method setMethod = map.get(field);
            if (setMethod != null) {
                setBaseObjectValue(target,entry.getValue(),field,setMethod,defaultDateTimeFormatter);
            }
        }
    }
    private static void setBaseObjectValue(BaseObject target,Object value,String field,Method setMethod,String... defaultDateTimeFormatter) throws Exception{
        if (value != null) {
            target.AddDirtyColumn(field);
            Class type = setMethod.getParameterTypes()[0];
            if (StringUtils.isEmpty(value)) {
                setMethod.invoke(target);
            } else {
                Object retValue = parseParameter(type, value,defaultDateTimeFormatter);
                setMethod.invoke(target, retValue);
            }
        }
    }
    private static void setObjectValue(Serializable target,Object value,String field,Method setMethod,String... defaultDateTimeFormatter) throws Exception{
        if (value != null) {
            Class type = setMethod.getParameterTypes()[0];
            if (!"java.lang.String".equalsIgnoreCase(type.getName()) && "".equals(value)) {
                setMethod.invoke(target);
            } else {
                Object retValue = parseParameter(type, value,defaultDateTimeFormatter);
                setMethod.invoke(target, retValue);
            }
        }
    }
    public static void convertToTarget(Object target, Object src,String... defaultDateTimeFormatter) throws Exception {
        if (target == null || src == null) {
            return;
        }
        Map<String, Method> targetMethodMap = ReflectUtils.returnSetMethods(target.getClass());
        Map<String, Method> sourceMethodMap = ReflectUtils.returnGetMethods(src.getClass());
        if(src instanceof Map) {
            Map<String,Object> vMap=(Map<String,Object>)src;
            Iterator<Map.Entry<String,Object>> set = vMap.entrySet().iterator();
            while (set.hasNext()) {
                Map.Entry<String,Object> entry=set.next();
                String field = entry.getKey();
                Method setMethod = targetMethodMap.get(field);
                if (setMethod != null) {
                    if(target instanceof BaseObject) {
                        setBaseObjectValue((BaseObject) target,entry.getValue(),field,setMethod);
                    } else{
                        if(targetMethodMap.containsKey(field)) {
                            Object retValue = parseParameter(targetMethodMap.get(field).getParameterTypes()[0], entry.getValue(),defaultDateTimeFormatter);
                            setObjectValue(targetMethodMap.get(field), target, retValue);
                        }
                    }
                }
            }
        }else {
            Iterator<Map.Entry<String,Method>> iter=sourceMethodMap.entrySet().iterator();
            while(iter.hasNext()){
                Map.Entry<String,Method> entry=iter.next();
                if (targetMethodMap.containsKey(entry.getKey()) && entry.getValue().getParameterTypes().length==0){
                    Object retValue = parseParameter(targetMethodMap.get(entry.getKey()).getParameterTypes()[0], entry.getValue().invoke(src,new Object[]{}),defaultDateTimeFormatter);
                    if(null!=retValue) {
                        setObjectValue(targetMethodMap.get(entry.getKey()), target, retValue);
                    }
                }
            }
        }
    }
    private static void setObjectValue(Method setMethod,Object target,Object value) throws Exception{
        setMethod.invoke(target,value);
    }


    public static Object parseParameter(Class type, Object strValue,String... defaultDateTimeFormatter) throws Exception {
        if (strValue == null) {
            return null;
        }
        DateTimeFormatter formatter=null;
        Object ret = null;
        if(!StringUtils.isEmpty(strValue)) {
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
                ret = BigDecimal.valueOf(Double.valueOf(strValue.toString()));
            }else if(Boolean.class.isAssignableFrom(type)){
                if(NumberUtils.isNumber(strValue.toString())){
                    ret=strValue.toString().equals(Const.VALID);
                }else{
                    ret=Boolean.valueOf(strValue.toString());
                }
            }
            else if (type.isAssignableFrom(java.util.Date.class) || type.isAssignableFrom(LocalDateTime.class) || type.isAssignableFrom(Timestamp.class)) {
                String value = strValue.toString().trim();
                if (defaultDateTimeFormatter.length == 0) {
                    formatter = getFormatter(value);
                } else {
                    formatter = DateTimeFormatter.ofPattern(defaultDateTimeFormatter[0]);
                }
                if (null != formatter) {
                    LocalDateTime localDateTime = LocalDateTime.parse(value, formatter);
                    if (type.isAssignableFrom(java.util.Date.class)) {
                        ret = new java.util.Date(localDateTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli());
                    } else if (type.isAssignableFrom(Timestamp.class)) {
                        ret = new Timestamp(localDateTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli());
                    } else if (type.isAssignableFrom(LocalDateTime.class)) {
                        ret = localDateTime;
                    }
                }

            } else if (type.isAssignableFrom(String.class)) {
                ret = strValue.toString();
            } else {

                if (type.isAssignableFrom(byte.class)) {
                    if (!strValue.toString().isEmpty()) {
                        Method method = type.getMethod("valueOf", String.class);
                        if (method != null) {
                            ret = method.invoke(Class.forName("java.lang.Byte"), strValue.toString());
                        }
                    }
                } else if (type.isAssignableFrom(byte[].class)) {
                    ret = strValue.toString().getBytes();
                } else {
                    if(Map.class.isAssignableFrom(type) && Map.class.isAssignableFrom(strValue.getClass())){

                    }else if(List.class.isAssignableFrom(type)){

                    }
                    ret = strValue;
                }
            }
        }
        return ret;
    }


    public static DateTimeFormatter getFormatter(String value){
        DateTimeFormatter retFormatter=null;
        if(isFormatterFit(value,ymdformatter)){
            retFormatter=ymdformatter;
        }else if(isFormatterFit(value,ymdSepformatter)){
            retFormatter=ymdSepformatter;
        }else if(isFormatterFit(value,ymdSecondformatter)){
            retFormatter=ymdSecondformatter;
        }else if(isFormatterFit(value,ymdSepSecondformatter)){
            retFormatter=ymdSepSecondformatter;
        }else if(isFormatterFit(value,ymdEupformatter)){
            retFormatter=ymdEupformatter;
        }
        return retFormatter;
    }
    private static boolean isFormatterFit(String value,DateTimeFormatter formatter){
        try{
            formatter.parse(value);
            return true;
        }catch (Exception ex){

        }
        return false;
    }

    public static Object convertStringToTargetObject(String value, DataSetColumnMeta meta, String defaultDateTimefromat) throws Exception {
        Object retObj;
        SimpleDateFormat dateformat = null;
        String dateformatstr = defaultDateTimefromat;
        if (dateformatstr == null || StringUtils.isEmpty(dateformatstr)) {
            dateformatstr = Const.DEFAULT_DATETIME_FORMAT;
        }
        dateformat = new SimpleDateFormat(dateformatstr);
        String columnType = meta.getColumnType();
        retObj = translateValue(value, columnType, meta.getColumnName(), dateformat);
        if (retObj == null && meta.getDefaultNullValue() != null) {
            retObj = meta.getDefaultNullValue();
        }
        return retObj;
    }

    public static Object convertStringToTargetObject(String value, String columnType, String columnName, String defaultDateTimefromat) throws Exception {
        Object retObj;
        SimpleDateFormat dateformat = null;
        String dateformatstr = defaultDateTimefromat;
        if (dateformatstr == null || StringUtils.isEmpty(dateformatstr)) {
            dateformatstr = Const.DEFAULT_DATETIME_FORMAT;
        }
        dateformat = new SimpleDateFormat(dateformatstr);
        retObj = translateValue(value, columnType, columnName, dateformat);
        return retObj;
    }

    public static Object wrapObjectByAutoDetect(Object object, String dateFormatStr) {
        Object retObj = null;
        if (object != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatStr != null && !dateFormatStr.isEmpty() ? dateFormatStr : "yyyy-MM-dd");
            Class clazz = object.getClass();
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
                            Date date = dateFormat.parse(object.toString());
                            retObj = new Timestamp(date.getTime());
                        } catch (ParseException ex) {
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

    private static Object translateValue(String value, String columnType, String columnName, SimpleDateFormat dateformat) throws Exception {
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
                retObj = dateformat.parse(value);
            } else if (columnType.equals(Const.META_TYPE_TIMESTAMP)) {
                retObj = new Timestamp(dateformat.parse(value).getTime());
            } else {
                retObj = value.toString();
            }
        } catch (Exception ex) {
            throw new RuntimeException("columnName =" + columnName + ",type=" + columnType + " with value=" + value + "failed! type mismatch,Please check!");
        }
        return retObj;
    }
}
