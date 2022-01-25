package com.robin.core.base.dao.handler;

import com.robin.core.base.reflect.MetaObject;

import java.util.Objects;

public interface MetaObjectHandler {
    void insertFill(MetaObject object);

    void updateFill(MetaObject object);

    default MetaObjectHandler setFieldValByName(String fieldName, Object value, MetaObject metaObject) {
        if (Objects.nonNull(value) && metaObject.hasSetter(fieldName)) {
            metaObject.setValue(fieldName, value);
        }
        return this;
    }
    default boolean containColumn(String fieldName,MetaObject metaObject){
        return metaObject.hasGetter(fieldName) && metaObject.hasSetter(fieldName);
    }

    default Object getFieldValByName(String fieldName, MetaObject metaObject) {
        return metaObject.hasGetter(fieldName) ? metaObject.getValue(fieldName) : null;
    }
}
