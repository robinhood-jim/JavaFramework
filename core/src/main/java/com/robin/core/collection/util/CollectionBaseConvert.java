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

import com.robin.core.base.dao.util.AnnotationRetriever;
import com.robin.core.base.dao.util.PropertyFunction;
import com.robin.core.base.exception.MissingConfigException;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CollectionBaseConvert {

    public static Map<String, List<Map<String, String>>> convertToMapByParentKey(List<Map<String, String>> listobj, String key) {
        return listobj.stream().collect(Collectors.groupingBy(f->f.get(key)));
    }


    public static Map<String, List<Map<String, Object>>> convertToMapByParentKeyWithObjVal(List<Map<String, Object>> listobj, String key) {
        return listobj.stream().collect(Collectors.groupingBy(f->f.get(key).toString()));
    }

    public static Map<String, Map<String, Object>> listObjectToMap(List<Map<String, Object>> listobj, String key) {
        return listobj.stream().collect(Collectors.toMap(f->f.get(key).toString(),Function.identity()));
    }

    public static Map<String, List<Map<String, String>>> convertToMapObjByParentKey(List<Map<String, String>> listobj, String key) {
        return listobj.stream().collect((Collectors.groupingBy(f->f.get(key))));
    }

    public static List<String> extractKeyValueByList(List<Map<String, String>> list, String key) {
        return list.stream().collect(Collectors.mapping(f->f.get(key).toString(),Collectors.toList()));
    }

    public static List<Object> extractKeyValueByListObj(List<Map<String, Object>> list, String key) {
        List<Object> retList = new ArrayList<>();
        for (Map<String, Object> map : list) {
            if (map.containsKey(key)) {
                retList.add(map.get(key));
            }
        }
        return retList;
    }

    public static List<String> extractKeyStringValueByListObj(List<Map<String, Object>> list, String key) {
        List<String> retList = new ArrayList<>();
        for (Map<String, Object> map : list) {
            if (map.containsKey(key)) {
                retList.add(map.get(key).toString());
            }
        }
        return retList;
    }

    public static <T> Map<String, List<T>> groupBy(List<T> list, Function<T, String> function) {
        Assert.notNull(list, "");
        Assert.isTrue(!CollectionUtils.isEmpty(list), "Collection is Empty");
        Assert.isTrue(!list.get(0).getClass().isPrimitive(), "Primitive type can not using this function!");
        return list.stream().collect(Collectors.groupingBy(function));
    }

    public static <T extends Serializable> Map<String, T> groupByUniqueKey(List<T> list, PropertyFunction<T, String> function) throws Exception {
        Assert.notNull(list, "");
        Assert.isTrue(!CollectionUtils.isEmpty(list), "Collection is Empty");
        Class<? extends Serializable> clazz = list.get(0).getClass();
        if (clazz.isPrimitive()) {
            throw new MissingConfigException("Primitive type can not using this function!");
        }
        if (!(list.get(0) instanceof HashMap)) {
            return list.stream().collect(Collectors.toMap(f -> function.apply(f), Function.identity()));
        } else {
            String columnName = AnnotationRetriever.getFieldName(function);
            return list.stream().collect(Collectors.toMap(f -> ((Map) f).get(columnName).toString(), Function.identity()));
        }

    }

    public static <T extends Comparable> List<Map.Entry<String, T>> sortMapByValue(Map<String, T> inputMap, final boolean order) {
        List<Map.Entry<String, T>> list = new LinkedList<>(inputMap.entrySet());
        Collections.sort(list, (Comparator<Map.Entry<String, ?>>) (o1, o2) -> {
            if (order) {
                return ((Comparable) o1.getValue()).compareTo(o2.getValue());

            } else {
                return ((Comparable) o2.getValue()).compareTo(o1.getValue());
            }
        });
        return list;
    }
}
