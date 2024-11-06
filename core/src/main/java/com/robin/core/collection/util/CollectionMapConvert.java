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
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.script.ScriptExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.script.Bindings;
import javax.script.CompiledScript;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class CollectionMapConvert {

    private CollectionMapConvert() {

    }

    /**
     * Convert ArrayList to Map by identify Column
     *
     * @param listobj     ArrayList must not Primitive and not HashMap
     * @param identityCol
     * @return
     * @throws Exception
     */
    public static <T> Map<String, T> convertListToMap(List<T> listobj, String identityCol) throws MissingConfigException,InvocationTargetException,IllegalAccessException {
        checkType(listobj);
        Map<String, T> retMap = new HashMap<>();
        Map<String, Method> methodMap = ReflectUtils.returnGetMethods(listobj.get(0).getClass());
        Method method = methodMap.get(identityCol);
        if (method != null) {
            for (T targerobj : listobj) {
                Object obj = method.invoke(targerobj, (Object) null);
                String value = obj.toString();
                if (obj instanceof Double) {
                    value = String.valueOf(((Double) obj).longValue());
                }
                if (obj instanceof Long) {
                    value = String.valueOf(((Long) obj).longValue());
                }
                retMap.put(value, targerobj);

            }
        } else {
            throw new MissingConfigException("identify column not exists in object!");
        }
        return retMap;
    }

    public static <T> List<T> convertToList(Map<String, T> mapobj) {
        List<T> retList = new ArrayList<>();
        for (T t : mapobj.values()) {
            retList.add(t);
        }
        return retList;
    }

    private static <T> void checkType(List<T> listobj) throws MissingConfigException {
        if (CollectionUtils.isEmpty(listobj)) {
            log.warn("ArrayList is Empty");
            throw new MissingConfigException("ArrayList is Empty!");
        }
        if (listobj.get(0).getClass().isPrimitive()) {
            throw new MissingConfigException("Primitive type can not using this function!");
        }
    }

    /**
     * Convert ArrayList to Map by identify Column
     *
     * @param listobj   ArrayList must not Primitive,can input HashMap
     * @param function
     * @param parentCol
     * @return Map<Key, List < T>>
     * @throws Exception
     */

    public static <T> Map<String, List<T>> convertToMapByParentKey(List<T> listobj,Function<T,String> function,String parentCol) throws InvocationTargetException,IllegalAccessException {
        checkType(listobj);
        Assert.isTrue(!ObjectUtils.isEmpty(function) || !ObjectUtils.isEmpty(parentCol),"");

        return listobj.stream().collect(Collectors.groupingBy(f->{
            if(f.getClass().isAssignableFrom(Map.class)){
                return ((Map)f).get(parentCol).toString();
            }else{
                return function.apply(f);
            }
        }));
    }
    public static  Map<String, List<Map<String,Object>>> convertToMapByParentMapKey(List<Map<String,Object>> listobj,String parentCol) throws InvocationTargetException,IllegalAccessException {
        checkType(listobj);
        return listobj.stream().collect(Collectors.groupingBy(f-> f.get(parentCol).toString()));
    }


    private static <T> void addMapToList(Map<String, List<T>> retMap, String key, T t) {
        if (!retMap.containsKey(key)) {
            List<T> list = new ArrayList<>();
            list.add(t);
            retMap.put(key, list);
        } else {
            retMap.get(key).add(t);
        }
    }

    /**
     * extract Key and List Value Map
     *
     * @param listobj   ArrayList must not Primitive and not HashMap
     * @param parentCol
     * @param valueCol
     * @return
     * @throws Exception
     */
    public static <T, P, V> Map<P, List<V>> getValuesByParentKey(List<T> listobj, Function<T,P> parentCol, Function<T,V> valueCol) throws Exception {
        checkType(listobj);
        Assert.isTrue(!CollectionUtils.isEmpty(listobj), "");
        return listobj.stream().collect(Collectors.groupingBy(parentCol,Collectors.mapping(valueCol,Collectors.toList())));
    }

    /**
     * same function like select from where
     *
     * @param listobj  ArrayList must not Primitive and not HashMap
     * @param filterColumn  select column function
     * @param colvalue select value
     * @return
     * @throws Exception
     */
    public static <T> List<T> filterListByColumnValue(List<T> listobj, Function<T,?> filterColumn, Object colvalue) {
        checkType(listobj);
        return listobj.stream().filter(f->filterColumn.apply(f)!=null && colvalue.equals(filterColumn.apply(f))).collect(Collectors.toList());
    }

    /**
     * select from using complex condition with script engine
     *
     * @param listobj
     * @param scriptType      script type (js/groovy/jython)
     * @param queryConditions script content return boolean
     * @return
     * @throws Exception
     */
    public static <T> List<T> filterListByColumnCondition(List<T> listobj, String scriptType, String queryConditions) throws Exception {
        checkType(listobj);
        CompiledScript script =ScriptExecutor.getInstance().returnScriptNoCache(scriptType, queryConditions);
        Bindings bindings = ScriptExecutor.getInstance().createBindings(scriptType);
        return listobj.stream().filter(f->{
            Map<String, Object> valueMap = new HashMap<>();
            try {
                bindings.clear();
                ConvertUtil.objectToMapObj(valueMap, f);
                bindings.putAll(valueMap);
                return (Boolean) script.eval(bindings);
            }catch (Exception ex){
                return false;
            }
        }).collect(Collectors.toList());

    }


    public static <T> String getColumnValueAppendBySeparate(List<T> listobj, Function<T,?> column, String separate) throws MissingConfigException,InvocationTargetException,IllegalAccessException {
        checkType(listobj);
        StringBuilder buffer = new StringBuilder();
        Assert.notNull(column,"");
        List<?> values= listobj.stream().collect(Collectors.mapping(column,Collectors.toList()));
        return StringUtils.join(values,separate);
    }

    public static <T,P> List<P> getValueListBySeparate(List<T> listobj, Function<T,P> column) throws MissingConfigException,InvocationTargetException,IllegalAccessException {
        List<String> retList = new ArrayList<>();
        checkType(listobj);
        return listobj.stream().collect(Collectors.mapping(column,Collectors.toList()));
    }

    public static <T> List<Map<String, Object>> getListMap(List<T> listobj) throws MissingConfigException,InvocationTargetException,IllegalAccessException {
        checkType(listobj);
        Map<String, Method> getMetholds = ReflectUtils.returnGetMethods(listobj.get(0).getClass());
        if (getMetholds.isEmpty()) {
            throw new MissingConfigException("target object contain no get methold!");
        }
        List<Map<String, Object>> retList = new ArrayList<>();
        for (T t : listobj) {
            Map<String, Object> retmap = new HashMap<>();
            for (Map.Entry<String, Method> entry : getMetholds.entrySet()) {
                Object obj = entry.getValue().invoke(t, null);
                if (obj != null) {
                    retmap.put(entry.getKey(), obj);
                }
            }
            retList.add(retmap);
        }
        return retList;
    }

    public static <T> List<T> mergeListFromNew(List<T> orgList, List<T> newList, String identifyCol) throws MissingConfigException,InvocationTargetException,IllegalAccessException {
        if (CollectionUtils.isEmpty(orgList) || CollectionUtils.isEmpty(newList)) {
            throw new MissingConfigException("Input ArrayList is Empty!");
        }
        Map<String, T> map = convertListToMap(newList, identifyCol);
        List<T> retList = new ArrayList<>();
        Method method = ReflectUtils.returnGetMethods(orgList.get(0).getClass()).get(identifyCol);
        if (method == null) {
            throw new MissingConfigException("identify column not exist in object");
        }
        for (T obj : orgList) {
            Object val = method.invoke(obj, (Object) null);
            if (map.get(val.toString()) != null) {
                retList.add(map.get(val.toString()));
            } else {
                retList.add(obj);
            }
        }
        return retList;
    }

}

