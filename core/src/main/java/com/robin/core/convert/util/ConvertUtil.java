/*
 * Copyright (c) 2015,wanchuan
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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ConvertUtil {

    private static Cache<String,Map<String,Method>> cachedGetMethod= CacheBuilder.newBuilder().maximumSize(200).expireAfterAccess(30, TimeUnit.MINUTES).build();
    private static Cache<String,Map<String,Method>> cachedSetMethod= CacheBuilder.newBuilder().maximumSize(200).expireAfterAccess(30, TimeUnit.MINUTES).build();


    public static void convertToTargetObj(Object target, Object src) throws Exception {
        if (target == null || src == null) return;

        Map<String, Method> srcmap = returnGetMethold(src.getClass());
        Map<String,Method> targetMap=returnSetMethold(target.getClass());

        Iterator<Map.Entry<String,Method>> iterator=srcmap.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<String,Method> entry=iterator.next();
            if(targetMap.containsKey(entry.getKey())){
                Object value=parseParamenter(targetMap.get(entry.getKey()).getParameterTypes()[0],srcmap.get(entry.getKey()).invoke(src,(Object[]) null));
                targetMap.get(entry.getKey()).invoke(target,new Object[]{value});
            }
        }
    }
    public static Map<String,Method> returnGetMethold(Class clazz){
        if(cachedGetMethod.getIfPresent(clazz.getCanonicalName())==null){
            Map<String,Method> map=new HashMap<>();
            Method[] methods=clazz.getMethods();
            for(Method method:methods){
                if(method.getName().startsWith("get")){
                    String name=method.getName().substring(3,4).toLowerCase()+method.getName().substring(4);
                    map.put(name,method);
                }
            }
            if(!map.isEmpty())
                cachedGetMethod.put(clazz.getCanonicalName(),map);
        }
        return cachedGetMethod.getIfPresent(clazz.getCanonicalName());
    }

    public static Map<String,Method> returnSetMethold(Class clazz){
        if(cachedSetMethod.getIfPresent(clazz.getCanonicalName())==null){
            Method[] methods=clazz.getMethods();
            Map<String,Method> map=new HashMap<>();
            for(Method method:methods){
                if(method.getName().startsWith("set")){
                    String name=method.getName().substring(3,4).toLowerCase()+method.getName().substring(4);
                    map.put(name,method);
                }
            }
            if(!map.isEmpty())
                cachedSetMethod.put(clazz.getCanonicalName(),map);
        }
        return cachedSetMethod.getIfPresent(clazz.getCanonicalName());
    }
    private static Method getMethodByName(Class clazz,String methodName){
        try{
            return clazz.getDeclaredMethod(methodName);
        }catch (NoSuchMethodException ex){

        }
        return null;
    }
    private static Method getMethodByName(Class clazz, String methodName, Type type){
        try{
            return clazz.getDeclaredMethod(methodName,(Class) type);
        }catch (NoSuchMethodException ex){

        }
        return null;
    }

    public static void objectToMap(Map<String, String> target, Object src) throws Exception {
        if (src == null || target == null) return;

        Map<String,Method> getMetholds=returnGetMethold(src.getClass());
        Iterator<Map.Entry<String,Method>> iterator=getMetholds.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<String,Method> entry=iterator.next();
            if(entry.getValue().getParameterTypes().length==0) {
                Object value = entry.getValue().invoke(src, (Object[]) null);
                target.put(entry.getKey(), value == null ? "" : value.toString().trim());
            }
        }
    }

    public static void objectToMapObj(Map<String, Object> target, Object src) throws Exception {
        if (src == null || target == null) return;
        Map<String,Method> getMetholds=returnGetMethold(src.getClass());
        Iterator<Map.Entry<String,Method>> iterator=getMetholds.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<String,Method> entry=iterator.next();
            if(entry.getValue().getParameterTypes().length==0) {
                Object value = entry.getValue().invoke(src, (Object[]) null);
                target.put(entry.getKey(), value);
            }
        }
    }

    public static void mapToObject(BaseObject target, Map<String, String> src) throws Exception {
        if (src == null || target == null)
            return;
        Iterator<Map.Entry<String, String>> it = src.entrySet().iterator();
        Map<String, Method> methodMap = returnSetMethold(target.getClass());
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            String key = entry.getKey();
            String value = entry.getValue();
            if (methodMap.containsKey(key)) {
                target.AddDirtyColumn(key);
                Class type = methodMap.get(key).getParameterTypes()[0];
                Object retValue = null;
                if (!type.getName().equalsIgnoreCase("java.lang.String") && value.equals(""))
                    retValue = null;
                else
                    retValue = parseParamenter(type, value);
                methodMap.get(key).invoke(target, new Object[]{retValue});
            }
        }

    }

    public static void mapToObject(Object target, Map<String, Object> src) throws Exception {
        if (src == null || target == null) return;

        Iterator<Map.Entry<String,Object>> it = src.entrySet().iterator();
        Map<String,Method> targetMap=returnSetMethold(target.getClass());
        while (it.hasNext()) {
            Map.Entry<String,Object> entry=it.next();
            String key = entry.getKey();
            Object value = entry.getValue();
            if (targetMap.containsKey(key)) {
                Class type = targetMap.get(key).getParameterTypes()[0];
                Object retValue = null;
                if (!type.getName().equalsIgnoreCase("java.lang.String") && value.equals("")) retValue = null;
                else retValue = parseParamenter(type, value);
                targetMap.get(key).invoke(target, new Object[]{retValue});
            }
        }
    }


    private static String wordCase(String value) {

        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }




    public static void convertToModelForUpdate(BaseObject target, BaseObject src) throws Exception {
        if (target == null || src == null) return;
        if (!target.getClass().equals(src.getClass())) throw new RuntimeException("");
        Map<String, Method> srcmap = returnGetMethold(src.getClass());
        Map<String,Method> targetMap=returnSetMethold(target.getClass());

        List<String> dirtyColumnList = src.getDirtyColumn();
        for (int i = 0; i < dirtyColumnList.size(); i++) {

            Method setMethod = targetMap.get(dirtyColumnList.get(i));
            if (setMethod != null) {
                Method getMethod = srcmap.get(dirtyColumnList.get(i));
                Object value = getMethod.invoke(src, (Object[]) null);
                if (value != null) {
                    setMethod.invoke(target, new Object[]{value});
                } else
                    setMethod.invoke(target, new Object[]{null});
            }

        }
    }



    public static void convertToModel(BaseObject target, Map<String, String> src) throws Exception {
        if (target == null || src == null) return;

        Map<String, Method> map = returnSetMethold(target.getClass());

        Iterator<Map.Entry<String,String>> set = src.entrySet().iterator();
        while (set.hasNext()) {
            Map.Entry<String,String> entry=set.next();
            String field = entry.getKey();
            Method setMethod = map.get(field);
            if (setMethod != null) {
                setBaseObjectValue(target,entry.getValue(),field,setMethod);
            }
        }
    }
    private static void setBaseObjectValue(BaseObject target,Object value,String field,Method setMethod) throws Exception{
        if (value != null) {
            target.AddDirtyColumn(field);
            Class type = setMethod.getParameterTypes()[0];
            if (!type.getName().equalsIgnoreCase("java.lang.String") && value.equals("")) {
                setMethod.invoke(target, new Object[]{null});
            } else {
                Object retValue = parseParamenter(type, value);
                setMethod.invoke(target, new Object[]{retValue});
            }
        }
    }
    public static void convertToTarget(Object target, Object src) throws Exception {
        if (target == null || src == null) return;
        Map<String, Method> targetMethodMap = returnSetMethold(target.getClass());
        Map<String, Method> sourceMethodMap = returnGetMethold(src.getClass());
        if(src instanceof Map) {
            Map<String,Object> vMap=(Map<String,Object>)src;
            Iterator<Map.Entry<String,Object>> set = vMap.entrySet().iterator();
            while (set.hasNext()) {
                Map.Entry<String,Object> entry=set.next();
                String field = entry.getKey();
                Method setMethod = targetMethodMap.get(field);
                if (setMethod != null) {
                    if(target instanceof BaseObject)
                        setBaseObjectValue((BaseObject) target,entry.getValue(),field,setMethod);
                    else{
                        if(targetMethodMap.containsKey(field)) {
                            Object retValue = parseParamenter(targetMethodMap.get(field).getParameterTypes()[0], entry.getValue());
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
                    Object retValue = parseParamenter(targetMethodMap.get(entry.getKey()).getParameterTypes()[0], entry.getValue().invoke(src,new Object[]{}));
                    setObjectValue(targetMethodMap.get(entry.getKey()), target, retValue);
                }
            }
        }
    }
    private static void setObjectValue(Method setMethod,Object target,Object value) throws Exception{
        setMethod.invoke(target,new Object[]{value});
    }


    public static Object parseParamenter(Class type, Object strValue) throws Exception {
        if (strValue == null) {
            return null;
        }
        String typeName = type.getName();
        if (type.equals(byte[].class)) {
            typeName = "byte";
        }
        Object ret = null;
        if (typeName.equals("int")) {
            ret = Integer.parseInt(strValue.toString());
        } else if (typeName.equals("long")) {
            ret = Long.parseLong(strValue.toString());
        } else if (typeName.equals("float")) {
            ret = Float.parseFloat(strValue.toString());
        } else if (typeName.equals("double")) {
            ret = Double.parseDouble(strValue.toString());
        } else if (typeName.startsWith("java.math.") || "java.util.Date" .equals(typeName)) {
            String value = strValue.toString().trim();
            if (value.indexOf(":") == -1)
                value += " 00:00:00";
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            ret = format.parse(value);

        } else if (typeName.equals("java.lang.String")) {
            ret = strValue.toString();
        } else {
            if (typeName.equals("java.sql.Timestamp")) {
                String value = strValue.toString().trim();
                int len = value.trim().length();
                if (len > 7 && len < 11) {
                    value = value + " 00:00:00.0";
                } else if (len > 11 && value.indexOf(".") == -1) {
                    value = value + ".0";
                }
                strValue = value;
            } else if (typeName.equals("java.sql.Date") && strValue != null) {
                String value = strValue.toString().trim();
                if (value.length() > 10) {
                    value = value.substring(0, 10);
                }
                strValue = value;
            }
            if (!typeName.equals("byte")) {
                if (!strValue.toString().isEmpty()) {
                    Method method = type.getMethod("valueOf", new Class[]{"java.lang.String" .getClass()});
                    if (method != null)
                        ret = method.invoke(Class.forName("java.lang.Byte"), new Object[]{strValue.toString()});
                } else {
                    ret = null;
                }
            } else {
                ret = strValue;
            }
        }
        return ret;
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
            if (value == null || StringUtils.isEmpty(value.trim()))
                return null;
            if (columnType.equals(Const.META_TYPE_INTEGER)) {
                retObj = Integer.valueOf(value);
            } else if (columnType.equals(Const.META_TYPE_BIGINT))
                retObj = Long.valueOf(value);
            else if (columnType.equals(Const.META_TYPE_NUMERIC)) {
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
