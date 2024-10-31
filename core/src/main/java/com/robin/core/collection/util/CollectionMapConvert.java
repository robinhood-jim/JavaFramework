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
    public static <T> Map<?, T> convertListToMap(List<T> listobj, Function<T, ?> identityCol) throws MissingConfigException, InvocationTargetException, IllegalAccessException {
        checkType(listobj);
        return listobj.stream().collect(Collectors.toMap(identityCol, Function.identity()));
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
     * @param parentCol
     * @return Map<Key, List < T>>
     * @throws Exception
     */

    public static Map<String, List<Map<String, Object>>> convertToMapByParentKey(List<Map<String, Object>> listobj, String parentCol) throws InvocationTargetException, IllegalAccessException {
        return listobj.stream().collect(Collectors.groupingBy(f -> f.get(parentCol).toString()));
    }

    public static <T, P> Map<P, List<Map<String, Object>>> convertToMapByParentKey(List<T> listobj, Function<T, P> pkColumn) {
        checkType(listobj);
        return listobj.stream().collect(Collectors.groupingBy(pkColumn, Collectors.mapping(f -> {
            try {
                Map<String, Object> map = new HashMap<>();
                ConvertUtil.objectToMapObj(map, f);
                return map;
            } catch (Exception ex) {

            }
            return null;
        }, Collectors.toList())));
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
    public static <T, U, P> Map<U, List<P>> getValuesByParentKey(List<T> listobj, Function<T, U> parentCol, Function<T, P> valueCol) throws Exception {
        checkType(listobj);
        return listobj.stream().collect(Collectors.groupingBy(parentCol, Collectors.mapping(valueCol, Collectors.toList())));
    }

    public static <T, P> Map<String, List<P>> getValuesByParentKey(List<T> listobj, Function<T, String> keyColumn, Function<T, P> valueColumn, Class<P> clazz) {
        checkType(listobj);
        Assert.isTrue(!CollectionUtils.isEmpty(listobj), "");
        return listobj.stream().collect(Collectors.groupingBy(keyColumn, Collectors.mapping(valueColumn, Collectors.toList())));
    }

    /**
     * same function like select from where
     *
     * @param listobj      ArrayList must not Primitive and not HashMap
     * @param filterColumn select column function
     * @param colvalue     select value
     * @return
     * @throws Exception
     */
    public static <T> List<T> filterListByColumnValue(List<T> listobj, Function<T, ?> filterColumn, Object colvalue) {
        checkType(listobj);
        return listobj.stream().filter(f -> filterColumn.apply(f) != null && colvalue.equals(filterColumn.apply(f))).collect(Collectors.toList());
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
        CompiledScript script = ScriptExecutor.getInstance().returnScriptNoCache(scriptType, queryConditions);
        Bindings bindings = ScriptExecutor.getInstance().createBindings(scriptType);
        return listobj.stream().filter(f -> {
            Map<String, Object> valueMap = new HashMap<>();
            try {
                bindings.clear();
                ConvertUtil.objectToMapObj(valueMap, f);
                bindings.putAll(valueMap);
                return (Boolean) script.eval(bindings);
            } catch (Exception ex) {
                return false;
            }
        }).collect(Collectors.toList());

    }


    public static <T> String getColumnValueAppendBySeparate(List<T> listobj, Function<T, ?> column, String separate) throws MissingConfigException, InvocationTargetException, IllegalAccessException {
        checkType(listobj);
        StringBuilder buffer = new StringBuilder();
        Assert.notNull(column, "");
        List<?> values = listobj.stream().collect(Collectors.mapping(column, Collectors.toList()));
        return StringUtils.join(values, separate);
    }

    public static <T> List<?> getValueListBySeparate(List<T> listobj, Function<T, ?> column) throws MissingConfigException, InvocationTargetException, IllegalAccessException {
        List<String> retList = new ArrayList<>();
        checkType(listobj);
        return listobj.stream().collect(Collectors.mapping(column, Collectors.toList()));
    }

    public static <T> List<Map<String, Object>> getListMap(List<T> listobj) throws MissingConfigException {
        checkType(listobj);
        Map<String, Method> getMetholds = ReflectUtils.returnGetMethods(listobj.get(0).getClass());
        if (getMetholds.isEmpty()) {
            throw new MissingConfigException("target object contain no get methold!");
        }

        return listobj.stream().map(f -> {
            try {
                Map<String, Object> map = new HashMap<>();
                ConvertUtil.objectToMapObj(map, f);
                return map;
            } catch (Exception ex) {
            }
            return null;
        }).collect(Collectors.toList());

    }

    public static <T> void mergeListFromNew(List<T> orgList, List<T> newList, Function<T, ?> identifyCol) throws MissingConfigException, InvocationTargetException, IllegalAccessException {
        if (CollectionUtils.isEmpty(orgList) || CollectionUtils.isEmpty(newList)) {
            throw new MissingConfigException("Input ArrayList is Empty!");
        }
        Map<?, T> map = convertListToMap(newList, identifyCol);
        orgList.stream().filter(f -> !map.containsKey(identifyCol.apply(f))).forEach(newList::add);

    }

}

