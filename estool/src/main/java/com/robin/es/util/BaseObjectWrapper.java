package com.robin.es.util;

import com.robin.core.base.dao.util.AnnotationRetriever;
import com.robin.core.base.dao.util.FieldContent;
import com.robin.core.convert.util.ConvertUtil;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class BaseObjectWrapper {
    public static <V> void extractValue(V obj, Map.Entry<String, Object> entry, Map<String, FieldContent> fieldsMap, List<FieldContent> fieldContents) throws Exception {
        String key = entry.getKey();
        if (fieldsMap.containsKey(key)) {
            Method method = fieldsMap.get(key).getSetMethod();
            Class<?> paramType = method.getParameterTypes()[0];
            method.invoke(obj, ConvertUtil.parseParameter(paramType, entry.getValue()));
        }
        if (key.equalsIgnoreCase("_id")) {
            Method method = AnnotationRetriever.getPrimaryField(fieldContents).getSetMethod();
            Class<?> paramType = method.getParameterTypes()[0];
            method.invoke(obj, ConvertUtil.parseParameter(paramType, entry.getValue()));
        }
    }
}
