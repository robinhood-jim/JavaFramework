package com.robin.es.util;

import com.robin.core.base.dao.util.AnnotationRetriever;
import com.robin.core.base.dao.util.FieldContent;
import com.robin.core.convert.util.ConvertUtil;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Map;

public class BaseObjectWrapper {
    public static <V> void extractValue(V obj, Map.Entry<String, Object> entry, Map<String, FieldContent> fieldsMap, List<FieldContent> fieldContents) throws Throwable {
        String key = entry.getKey();
        if (fieldsMap.containsKey(key)) {
            MethodHandle method = fieldsMap.get(key).getSetMethod();
            Class<?> paramType = method.type().parameterType(1);
            method.bindTo(obj).invoke(ConvertUtil.parseParameter(paramType, entry.getValue()));
        }
        if (key.equalsIgnoreCase("_id")) {
            MethodHandle method = AnnotationRetriever.getPrimaryField(fieldContents).getSetMethod();
            Class<?> paramType = method.type().parameterType(1);
            method.bindTo(obj).invoke(ConvertUtil.parseParameter(paramType, entry.getValue()));
        }
    }
}
