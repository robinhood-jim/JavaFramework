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

import com.robin.core.base.model.BaseObject;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ConvertUtil {


    private static Logger logger = LoggerFactory.getLogger(ConvertUtil.class);

    public static void convertToWeb(Object target, Object src) throws Exception {
        if (target == null || src == null) return;

        Map<String, Method> map = getAllSetMethodNames(target);

        Method methods[] = src.getClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            String method = methods[i].getName();
            if (method.startsWith("get") && !"getClass".equals(method)) {
                Method setMethod = map.get(method.replaceFirst("get", "set"));
                if (setMethod != null) {
                    Object value = methods[i].invoke(src, (Object[]) null);
                    if (value != null) {
                        setMethod.invoke(target, new Object[]{value.toString()});
                    } else {
                        setMethod.invoke(target, new Object[]{""});
                    }
                }
            }
        }
    }

    public static void objectToMap(Map<String, String> target, Object src) throws Exception {
        if (src == null || target == null) return;

        Method methods[] = src.getClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            String method = methods[i].getName();
            if (method.startsWith("get") && !"getClass".equals(method)) {
                String key = method.replaceFirst("get", "");
                key = key.substring(0, 1).toLowerCase() + key.substring(1);
                Object value = methods[i].invoke(src, (Object[]) null);
                target.put(key, value == null ? "" : value.toString().trim());
            }
        }
    }

    public static void objectToMapObj(Map<String, Object> target, Object src) throws Exception {
        if (src == null || target == null) return;

        Method methods[] = src.getClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            String method = methods[i].getName();
            if (method.startsWith("get") && !"getClass".equals(method)) {
                String key = method.replaceFirst("get", "");
                key = key.substring(0, 1).toLowerCase() + key.substring(1);
                Object value = methods[i].invoke(src, (Object[]) null);
                target.put(key, value == null ? "" : value);
            }
        }
    }

    public static void mapToObject(BaseObject target, Map<String, String> src) throws Exception {
        if (src == null || target == null)
            return;
        Iterator<String> it = src.keySet().iterator();
        Method methods[] = target.getClass().getMethods();
        while (it.hasNext()) {
            String key = (String) it.next();
            String value = src.get(key);
            String methordName = "set" + key.substring(0, 1).toUpperCase() + key.substring(1);
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equalsIgnoreCase(methordName)) {
                    target.AddDirtyColumn(key);
                    Class type = methods[i].getParameterTypes()[0];
                    Object retValue = null;
                    if (!type.getName().equalsIgnoreCase("java.lang.String") && value.equals(""))
                        retValue = null;
                    else
                        retValue = parseParamenter(type, value);
                    methods[i].invoke(target, new Object[]{retValue});
                    break;
                }
            }
        }

    }

    public static void mapToObject(Object target, Map<String, Object> src) throws Exception {
        if (src == null || target == null) return;

        Iterator it = src.keySet().iterator();
        Method methods[] = target.getClass().getMethods();
        while (it.hasNext()) {
            String key = (String) it.next();
            String value = src.get(key).toString();
            String methordName = "set" + key.substring(0, 1).toUpperCase() + key.substring(1);
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equalsIgnoreCase(methordName)) {
                    Class type = methods[i].getParameterTypes()[0];
                    Object retValue = null;
                    if (!type.getName().equalsIgnoreCase("java.lang.String") && value.equals("")) retValue = null;
                    else retValue = parseParamenter(type, value);
                    methods[i].invoke(target, new Object[]{retValue});
                }
            }
        }
    }

    public static void mapToObject(Object target, Map<String, String> src, boolean idInclude) throws Exception {
        if (src == null || target == null) return;

        Iterator it = src.keySet().iterator();
        Method methods[] = target.getClass().getMethods();
        while (it.hasNext()) {
            String key = (String) it.next();
            if (!idInclude && key.equals("id")) continue;
            String value = src.get(key);
            String methordName = "set" + key.substring(0, 1).toUpperCase() + key.substring(1);
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equalsIgnoreCase(methordName)) {
                    Class type = methods[i].getParameterTypes()[0];
                    Object retValue = null;
                    if (!type.getName().equalsIgnoreCase("java.lang.String") && value.equals("")) retValue = null;
                    else retValue = parseParamenter(type, value);
                    methods[i].invoke(target, new Object[]{retValue});
                }
            }
        }
    }

    private static String wordCase(String value) {

        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }


    public static void convertToModel(Object target, Object src) throws Exception {
        if (target == null || src == null) return;

        Map<String, Method> map = getAllSetMethodNames(target);

        Method methods[] = src.getClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            String method = methods[i].getName();
            if (method.startsWith("get") && !"getClass".equals(method)) {
                Method setMethod = map.get(method.replaceFirst("get", "set"));
                if (setMethod != null) {
                    Object value = methods[i].invoke(src, (Object[]) null);
                    if (value != null) {
                        setMethod.invoke(target, new Object[]{value});
                    }
                }
            }
        }
    }

    public static void convertToModelForUpdateNew(BaseObject target, BaseObject src) throws Exception {
        if (target == null || src == null) return;
        if (!target.getClass().equals(src.getClass())) throw new RuntimeException("");
        Map<String, Method> map = getAllSetMethodNames(target);

        List<String> dirtyColumnList = src.getDirtyColumn();
        for (int i = 0; i < dirtyColumnList.size(); i++) {
            String method = dirtyColumnList.get(i).substring(0, 1).toUpperCase() + dirtyColumnList.get(i).substring(1, dirtyColumnList.get(i).length());
            Method setMethod = map.get("set" + method);
            if (setMethod != null) {
                Method getMethod = src.getClass().getMethod("get" + method, (Class[]) null);
                Object value = getMethod.invoke(src, (Object[]) null);
                if (value != null) {
                    setMethod.invoke(target, new Object[]{value});
                } else
                    setMethod.invoke(target, new Object[]{null});
            }

        }
    }

    public static void convertToModelForUpdate(Object target, Object src) throws Exception {
        if (target == null || src == null) return;
        if (!target.getClass().equals(src.getClass())) throw new RuntimeException("");
        try {
            Map<String, Method> map = getAllSetMethodNames(target);

            Method methods[] = src.getClass().getMethods();
            for (int i = 0; i < methods.length; i++) {
                String method = methods[i].getName();
                if (method.startsWith("get") && !"getClass".equals(method) && !"getId".equalsIgnoreCase(method)) {
                    Method setMethod = map.get(method.replaceFirst("get", "set"));
                    if (setMethod != null) {
                        logger.info("setMethod Name" + setMethod.getName());
                        Object value = methods[i].invoke(src, (Object[]) null);

                        if (value != null) {
                            logger.info("getValue=" + value.toString());
                            setMethod.invoke(target, new Object[]{value});
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static void convertToModel(Object target, Map<String, Object> src) throws Exception {
        if (target == null || src == null) return;

        Map<String, Method> map = getAllSetMethodNames(target);

        Iterator set = src.keySet().iterator();
        while (set.hasNext()) {
            String method = (String) set.next();
            Method setMethod = map.get("set" + wordCase(method));
            if (setMethod != null) {
                Object value = src.get(method);
                if (value != null) {
                    Class type = setMethod.getParameterTypes()[0];
                    if (!type.getName().equalsIgnoreCase("java.lang.String") && value.equals("")) {
                        setMethod.invoke(target, new Object[]{null});
                    } else {
                        Object retValue = parseParamenter(type, value);
                        setMethod.invoke(target, new Object[]{retValue});
                    }
                }
            }
        }
    }

    public static void convertToPool(Object target, Object src) throws Exception {
        if (target == null || src == null) return;

        Map<String, Method> map = getAllSetMethodNames(target);

        Method methods[] = src.getClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            String method = methods[i].getName();
            if (method.startsWith("get") && !"getClass".equals(method)) {
                Method setMethod = map.get(method.replaceFirst("get", "set"));
                if (setMethod != null) {
                    Object value = methods[i].invoke(src, (Object[]) null);
                    if (value != null) {
                        Class type = setMethod.getParameterTypes()[0];
                        Object retValue = parseParamenter(type, value);
                        setMethod.invoke(target, new Object[]{retValue});
                    }
                }
            }
        }
    }

    private static Map<String, Method> getAllSetMethodNames(Object source) throws Exception {
        Method methods[] = source.getClass().getMethods();
        Map<String, Method> map = new HashMap<String, Method>();

        for (int i = 0; i < methods.length; i++) {
            String method = methods[i].getName();
            if (method.startsWith("set")) {
                map.put(method, methods[i]);
            }
        }
        return map;
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
        if (type.isPrimitive()) {
            if ("int".equals(typeName)) type = Class.forName("java.lang.Integer");
            else if ("long".equals(typeName)) type = Class.forName("java.lang.Long");
            else if ("float".equals(typeName)) type = Class.forName("java.lang.Float");
            else if ("double".equals(typeName)) type = Class.forName("java.lang.Double");
            else if ("boolean".equals(typeName)) type = Class.forName("java.lang.Boolean");
            else if ("char".equals(typeName)) type = Class.forName("java.lang.Character");
            else if ("byte".equals(typeName)) type = Class.forName("java.lang.Byte");
        }

        if (typeName.startsWith("java.math.") || "java.util.Date".equals(typeName)) {
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
                    Method method = type.getMethod("valueOf", new Class[]{"java.lang.String".getClass()});
                    if (method != null)
                        ret = method.invoke(type, new Object[]{strValue.toString()});
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
        Object retObj=null;
        if (object != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatStr == null && !dateFormatStr.isEmpty() ? dateFormatStr : "yyyy-MM-dd");
            Class clazz = object.getClass();
            if (object.getClass().isPrimitive()) {
                if (clazz.equals(String.class)) {
                    if (NumberUtils.isNumber(object.toString())) {
                        Number number = NumberUtils.createNumber(object.toString());
                        if (Math.ceil(number.doubleValue()) == number.longValue()) {
                            if (number.longValue() < Integer.MAX_VALUE) {
                                retObj=number.intValue();
                            }else{
                                retObj=number.longValue();
                            }
                        }else{
                            retObj=number.doubleValue();
                        }
                    }else{
                        try {
                            Date date=dateFormat.parse(object.toString());
                            retObj=new Timestamp(date.getTime());
                        }catch (ParseException ex){
                            retObj=object;
                        }
                    }
                }else{
                    retObj=object;
                }
            } else {
                retObj=object;
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
