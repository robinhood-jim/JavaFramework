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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@SuppressWarnings("unused")
public class CollectionMapConvert {

    private CollectionMapConvert() {

    }

    /**
     *
     * @param listobj
     * @param identityCol
     * @return
     * @param <T>
     * @param <P>
     * @throws MissingConfigException
     */
    public static <T, P> Map<P, T> convertListToMap(List<T> listobj, Function<T, P> identityCol) throws MissingConfigException {
        checkType(listobj);
        return listobj.stream().collect(Collectors.toMap(identityCol, Function.identity()));
    }

    public static <T> List<T> convertToList(Map<String, T> mapobj) {
        List<T> retList = new ArrayList<>();
        retList.addAll(mapobj.values());
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
     *
     * @param listobj
     * @param parentCol
     * @return
     */
    public static Map<String, List<Map<String, Object>>> convertToMapByParentKey(List<Map<String, Object>> listobj, String parentCol) {
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
     *
     * @param listobj
     * @param parentCol
     * @param valueCol
     * @return
     * @param <T>
     * @param <U>
     * @param <P>
     */
    public static <T, U, P> Map<U, List<P>> getValuesByParentKey(List<T> listobj, Function<T, U> parentCol, Function<T, P> valueCol) {
        checkType(listobj);
        return listobj.stream().collect(Collectors.groupingBy(parentCol, Collectors.mapping(valueCol, Collectors.toList())));
    }

    public static <T, P> Map<String, List<P>> getValuesByParentKey(List<T> listobj, Function<T, String> keyColumn, Function<T, P> valueColumn, Class<P> clazz) {
        checkType(listobj);
        Assert.isTrue(!CollectionUtils.isEmpty(listobj), "");
        return listobj.stream().collect(Collectors.groupingBy(keyColumn, Collectors.mapping(valueColumn, Collectors.toList())));
    }

    /**
     *
     * @param listobj
     * @param filterColumn
     * @param colvalue
     * @return
     * @param <T>
     */
    public static <T> List<T> filterListByColumnValue(List<T> listobj, Function<T, ?> filterColumn, Object colvalue) {
        checkType(listobj);
        return listobj.stream().filter(f -> filterColumn.apply(f) != null && colvalue.equals(filterColumn.apply(f))).collect(Collectors.toList());
    }

    /**
     *
     * @param listobj
     * @param scriptType
     * @param queryConditions
     * @return
     * @param <T>
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


    public static <T> String getColumnValueAppendBySeparate(List<T> listobj, Function<T, ?> column, String separate) throws MissingConfigException {
        checkType(listobj);
        StringBuilder buffer = new StringBuilder();
        Assert.notNull(column, "");
        List<?> values = listobj.stream().map(column).collect(Collectors.toList());
        return StringUtils.join(values, separate);
    }

    public static <T, P> List<P> getValueListBySeparate(List<T> listobj, Function<T, P> column) throws MissingConfigException {
        List<String> retList = new ArrayList<>();
        checkType(listobj);
        return listobj.stream().map(column).collect(Collectors.toList());
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

    public static <T, P> void mergeListFromNew(List<T> orgList, List<T> newList, Function<T, P> identifyCol) throws MissingConfigException {
        if (CollectionUtils.isEmpty(orgList) || CollectionUtils.isEmpty(newList)) {
            throw new MissingConfigException("Input ArrayList is Empty!");
        }
        Map<P, T> map = convertListToMap(newList, identifyCol);
        orgList.stream().filter(f -> !map.containsKey(identifyCol.apply(f))).forEach(newList::add);
    }
}

