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
package com.robin.core.collection.util;

import com.robin.core.base.exception.MissingConfigException;
import com.robin.core.base.reflect.ReflectUtils;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.script.ScriptExecutor;

import javax.script.Bindings;
import javax.script.CompiledScript;
import java.lang.reflect.Method;
import java.util.*;

public class CollectionMapConvert<T> {

    private CollectionMapConvert() {

    }

    /**
     * Convert ArrayList to Map by identify Column
     * @param listobj ArrayList must not Primitive and not HashMap
     * @param identityCol
     * @return
     * @throws Exception
     */
    public static <T> Map<String, T> convertListToMap(List<T> listobj, String identityCol) throws Exception {
        checkType(listobj);
        Map<String, T> retMap = new HashMap<String, T>();
        Map<String, Method> methodMap = ReflectUtils.returnGetMethods(listobj.get(0).getClass());
        Method method = methodMap.get(identityCol);
        if (method != null) {
            for (int i = 0; i < listobj.size(); i++) {
                T targerobj = listobj.get(i);

                Object obj = method.invoke(targerobj, null);
                String value = obj.toString();
                if (obj instanceof Double) {
                    value = String.valueOf(((Double) obj).longValue());
                }
                if (obj instanceof Long) {
                    value = String.valueOf(((Long) obj).longValue());
                }
                retMap.put(value, listobj.get(i));

            }
        } else {
            throw new MissingConfigException("identify column not exists in object!");
        }
        return retMap;
    }

    public static <T> List<T> convertToList(Map<String, T> mapobj) throws Exception {
        List<T> retList = new ArrayList<T>();
        Iterator<T> iter = mapobj.values().iterator();
        while (iter.hasNext()) {
            retList.add(iter.next());
        }
        return retList;
    }
    private static <T> void checkType(List<T> listobj) throws MissingConfigException{
        if (listobj == null || listobj.size() == 0) {
            throw new MissingConfigException("ArrayList is Empty!");
        }
        if (listobj.get(0).getClass().isPrimitive()) {
            throw new MissingConfigException("Primitive type can not using this function!");
        }
    }

    /**
     * Convert ArrayList to Map by identify Column
     * @param listobj ArrayList must not Primitive,can input HashMap
     * @param parentCol
     * @return  Map<Key,List<T>>
     * @throws Exception
     */

    public static <T> Map<String, List<T>> convertToMapByParentKey(List<T> listobj, String parentCol) throws Exception {
        checkType(listobj);
        Method method = null;
        if (!(listobj.get(0) instanceof Map)) {
            method = ReflectUtils.returnGetMethods(listobj.get(0).getClass()).get(parentCol);
            if (method == null) {
                throw new MissingConfigException("parent column not exists in object!");
            }
        }
        Map<String, List<T>> retMap = new HashMap<String, List<T>>();
        for (T t : listobj) {
            Object targerobj = t;
            Object obj = null;
            if (t instanceof Map) {
                obj = ((Map) t).get(parentCol);
            } else {
                obj = method.invoke(targerobj, null);
            }
            if (obj == null) {
                addMapToList(retMap, "NULL", t);
            } else {
                addMapToList(retMap, obj.toString(), t);
            }
        }

        return retMap;
    }

    private static <T> void addMapToList(Map<String, List<T>> retMap, String key, T t) {
        if (!retMap.containsKey(key)) {
            List<T> list = new ArrayList<T>();
            list.add(t);
            retMap.put(key, list);
        } else {
            retMap.get(key).add(t);
        }
    }

    /**
     * extract Key and List Value Map
     * @param listobj  ArrayList must not Primitive and not HashMap
     * @param parentCol
     * @param valueCol
     * @return
     * @throws Exception
     */
    public static <T> Map<String, List<Object>> getValuesByParentKey(List<T> listobj, String parentCol, String valueCol) throws Exception {
        checkType(listobj);

        Map<String, List<Object>> retMap = new HashMap<String, List<Object>>();

        Map<String, Method> getMetholds = ReflectUtils.returnGetMethods(listobj.get(0).getClass());
        Method method = getMetholds.get(parentCol);
        Method method1 = getMetholds.get(valueCol);
        if (method == null || method1 == null) {
            throw new MissingConfigException("parent column or value column not exist in object");
        }
        for (T t : listobj) {
            Object value = method.invoke(t, null);
            Object targetValue = method1.invoke(t, null);
            if (value == null) {
                value = "NULL";
            }
            if (targetValue != null) {
                if (retMap.get(value) == null) {
                    List<Object> list = new ArrayList<Object>();
                    list.add(targetValue);
                    retMap.put(value.toString(), list);
                } else {
                    retMap.get(value.toString()).add(targetValue);
                }
            }
        }
        return retMap;
    }

    /**
     * same function like select from where
     * @param listobj ArrayList must not Primitive and not HashMap
     * @param colName select column
     * @param colvalue  select value
     * @return
     * @throws Exception
     */
    public static <T> List<T> filterListByColumnValue(List<T> listobj, String colName, Object colvalue) throws Exception {
        List<T> retList = new ArrayList<T>();
        checkType(listobj);
        Method method = ReflectUtils.returnGetMethods(listobj.get(0).getClass()).get(colName);
        if (method == null) {
            throw new MissingConfigException("parent column or value column not exist in object");
        }

        for (T t : listobj) {
            Object value = method.invoke(t, null);
            if (value != null && value.equals(colvalue)) {
                retList.add(t);
            }
        }
        return retList;
    }

    /**
     * select from using complex condition with script engine
     * @param listobj
     * @param scriptType script type (js/groovy/jython)
     * @param queryConditions script content return boolean
     * @return
     * @throws Exception
     */
    public static <T> List<T> filterListByColumnCondition(List<T> listobj,String scriptType, String queryConditions) throws Exception {
        List<T> retList = new ArrayList<T>();
        checkType(listobj);
        Map<String, Method> getMetholds = ReflectUtils.returnGetMethods(listobj.get(0).getClass());
        if (getMetholds == null) {
            throw new MissingConfigException("object does not have get method");
        }
        if(SpringContextHolder.getBean(ScriptExecutor.class)==null){
            throw new MissingConfigException("must use in spring context!");
        }
        CompiledScript script=SpringContextHolder.getBean(ScriptExecutor.class).returnScriptNoCache(scriptType,queryConditions);
        Bindings bindings=SpringContextHolder.getBean(ScriptExecutor.class).createBindings(scriptType);

        for (T t : listobj) {
            Map<String,Object> valueMap=new HashMap<>();
            ConvertUtil.objectToMapObj(valueMap,t);
            bindings.putAll(valueMap);
            boolean tag=(Boolean) script.eval(bindings);
            if(tag){
                retList.add(t);
            }
        }
        return retList;
    }


    public static <T> String getColumnValueAppendBySeparater(List<T> listobj, String colName, String separate) throws Exception {
        checkType(listobj);
        StringBuilder buffer = new StringBuilder();
        Method method = ReflectUtils.returnGetMethods(listobj.get(0).getClass()).get(colName);
        if (method == null) {
            throw new MissingConfigException("column not exist in object");
        }

        for (int i = 0; i < listobj.size(); i++) {
            Object targerobj = listobj.get(i);
            if (method != null) {
                Object obj = method.invoke(targerobj, null);
                String value = obj != null ? obj.toString() : "";
                buffer.append(value);
                if (i != listobj.size() - 1) {
                    buffer.append(separate);
                }
            }
        }

        return buffer.toString();
    }

    public static <T> List<String> getValueListBySeparater(List<T> listobj, String colName) throws Exception {
        List<String> retList = new ArrayList<String>();
        checkType(listobj);
        Method method = ReflectUtils.returnGetMethods(listobj.get(0).getClass()).get(colName);
        if (method == null) {
            throw new MissingConfigException("column not exist in object");
        }
        for (int i = 0; i < listobj.size(); i++) {
            Object obj = method.invoke(listobj.get(i), null);
            String value = obj.toString();
            retList.add(value);
        }
        return retList;
    }

    public static <T> List<Map<String, Object>> getListMap(List<T> listobj) throws Exception {
        checkType(listobj);
        Map<String, Method> getMetholds = ReflectUtils.returnGetMethods(listobj.get(0).getClass());
        if(getMetholds.isEmpty()){
            throw new MissingConfigException("target object contain no get methold!");
        }
        List<Map<String, Object>> retList = new ArrayList<>();
        for (T t : listobj) {
            Map<String, Object> retmap = new HashMap<>();
            Iterator<Map.Entry<String,Method>> iter=getMetholds.entrySet().iterator();
            while(iter.hasNext()){
                Map.Entry<String,Method> entry=iter.next();
                Object obj=entry.getValue().invoke(t,null);
                if(obj!=null){
                    retmap.put(entry.getKey(),obj);
                }
            }
            retList.add(retmap);
        }
        return retList;
    }

    public static <T> List<T> mergeListFromNew(List<T> orgList, List<T> newList, String identifyCol) throws Exception {
        if(orgList==null || newList==null || orgList.size()==0 || newList.size()==0){
            throw new MissingConfigException("Input ArrayList is Empty!");
        }
        Map<String, T> map = convertListToMap(newList, identifyCol);
        List<T> retList = new ArrayList<>();
        Method method=ReflectUtils.returnGetMethods(orgList.get(0).getClass()).get(identifyCol);
        if (method == null) {
            throw new MissingConfigException("identify column not exist in object");
        }
        for (T obj : orgList) {
            Object val = method.invoke(obj, null);
            if (map.get(val.toString()) != null) {
                retList.add(map.get(val.toString()));
            } else {
                retList.add(obj);
            }
        }
        return retList;
    }

}

