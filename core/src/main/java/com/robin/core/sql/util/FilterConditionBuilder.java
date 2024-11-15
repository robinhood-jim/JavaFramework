package com.robin.core.sql.util;

import com.robin.core.base.dao.util.AnnotationRetriever;
import com.robin.core.base.dao.util.FieldContent;
import com.robin.core.base.dao.util.PropertyFunction;
import com.robin.core.base.exception.ConfigurationIncorrectException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.util.Const;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

public class FilterConditionBuilder {
    private List<FilterCondition> conditions = new ArrayList<>();

    private Const.LINKOPERATOR linkOper = Const.LINKOPERATOR.LINK_AND;

    private Class<? extends BaseObject> mappingClass;

    public FilterConditionBuilder(Class<? extends BaseObject> mappingClass) {
        this.mappingClass = mappingClass;
    }

    public FilterConditionBuilder linkOper(Const.LINKOPERATOR linkOper) {
        this.linkOper = linkOper;
        return this;
    }

    public FilterConditionBuilder withCondition(FilterCondition filterCondition) {
        conditions.add(filterCondition);
        return this;
    }

    public FilterConditionBuilder eq(String columnName, Object object) {
        conditions.add(new FilterCondition(columnName, Const.OPERATOR.EQ, object));
        return this;
    }

    public FilterConditionBuilder eq(String columnName, Object object, String columnType) {
        conditions.add(new FilterCondition(columnName, columnType, Const.OPERATOR.EQ, object));
        return this;
    }

    public <T extends BaseObject> FilterCondition eq(PropertyFunction<T, ?> function, Object object) {
        String fieldName = AnnotationRetriever.getFieldName(function);
        Map<String, FieldContent> map1 = AnnotationRetriever.getMappingFieldsMapCache(mappingClass);
        String columnType = AnnotationRetriever.getFieldType(function);
        return Optional.ofNullable(map1.get(fieldName)).map(f -> {
            FilterCondition condition = new FilterCondition(f.getFieldName(), Const.OPERATOR.EQ);
            condition.setMappingClass(mappingClass);
            condition.setValue(object);
            condition.setColumnType(columnType);
            return condition;
        }).orElseGet(() -> {
            throw new ConfigurationIncorrectException("class " + mappingClass.getCanonicalName() + " can not parse");
        });
    }

    public <T extends BaseObject> FilterConditionBuilder addEq(PropertyFunction<T, ?> function, Object object) {
        String fieldName = AnnotationRetriever.getFieldName(function);
        Map<String, FieldContent> map1 = AnnotationRetriever.getMappingFieldsMapCache(mappingClass);
        String columnType = AnnotationRetriever.getFieldType(function);
        Optional.ofNullable(map1.get(fieldName)).map(f -> {
            FilterCondition condition = new FilterCondition(f.getFieldName(), Const.OPERATOR.EQ);
            condition.setMappingClass(mappingClass);
            condition.setValue(object);
            condition.setColumnType(columnType);
            conditions.add(condition);
            return f;
        }).orElseGet(() -> {
            throw new ConfigurationIncorrectException("class " + mappingClass.getCanonicalName() + " can not parse");
        });
        return this;
    }

    public <T extends BaseObject> FilterConditionBuilder addIn(String columnName, String columnType, List<?> objects) {
        conditions.add(in(columnName, columnType, objects));
        return this;
    }

    public FilterCondition in(String columnName, String columnType, List<?> objects) {
        FilterCondition condition = new FilterCondition(columnName, Const.OPERATOR.IN);
        condition.setValues(objects);
        condition.setColumnType(columnType);
        return condition;
    }
    public FilterCondition in(String columnName,FilterCondition inClause){
        FilterCondition condition = new FilterCondition(columnName, Const.OPERATOR.IN);
        condition.setCondition(inClause);
        return condition;
    }

    public <T extends BaseObject> FilterConditionBuilder addIn(String columnName, List<?> objects, String columnType) {
        FilterCondition condition = new FilterCondition(columnName, Const.OPERATOR.IN);
        condition.setValues(objects);
        condition.setColumnType(columnType);
        conditions.add(condition);
        return this;
    }

    public <T extends BaseObject> FilterConditionBuilder addIn(PropertyFunction<T, ?> function, Class<T> clazz, List<?> objects) {
        String fieldName = AnnotationRetriever.getFieldName(function);
        String columnType = AnnotationRetriever.getFieldType(function);
        Map<String, FieldContent> map1 = AnnotationRetriever.getMappingFieldsMapCache(clazz);
        Optional.ofNullable(map1.get(fieldName)).map(f -> {
            conditions.add(in(f.getFieldName(), columnType, objects));
            return f;
        }).orElseGet(() -> {
            throw new ConfigurationIncorrectException("class " + clazz.getCanonicalName() + " can not parse");
        });
        return this;
    }

    public FilterCondition notIn(String columnName, String columnType, List<?> objects) {
        FilterCondition condition = new FilterCondition(columnName, Const.OPERATOR.NOTIN);
        condition.setValues(objects);
        condition.setColumnType(columnType);
        return condition;
    }
    public FilterCondition notIn(String columnName, FilterCondition clause) {
        FilterCondition condition = new FilterCondition(columnName, Const.OPERATOR.NOTIN);
        condition.setConditions(Arrays.stream(new FilterCondition[]{clause}).collect(Collectors.toList()));
        return condition;
    }

    public <T extends BaseObject> FilterConditionBuilder addIn(PropertyFunction<T, ?> function,FilterCondition inClause){
        String fieldName = AnnotationRetriever.getFieldName(function);
        String columnType = AnnotationRetriever.getFieldType(function);
        Map<String, FieldContent> map1 = AnnotationRetriever.getMappingFieldsMapCache(mappingClass);
        Optional.ofNullable(map1.get(fieldName)).map(f -> {
            conditions.add(in(f.getFieldName(), inClause));
            return f;
        }).orElseGet(() -> {
            throw new ConfigurationIncorrectException("class " + mappingClass.getCanonicalName() + " can not parse");
        });
        return this;
    }
    public <T extends BaseObject> FilterConditionBuilder addNotIn(PropertyFunction<T, ?> function,FilterCondition inClause){
        String fieldName = AnnotationRetriever.getFieldName(function);
        Map<String, FieldContent> map1 = AnnotationRetriever.getMappingFieldsMapCache(mappingClass);
        Optional.ofNullable(map1.get(fieldName)).map(f -> {
            conditions.add(notIn(f.getFieldName(), inClause));
            return f;
        }).orElseGet(() -> {
            throw new ConfigurationIncorrectException("class " + mappingClass.getCanonicalName() + " can not parse");
        });
        return this;
    }

    public <T extends BaseObject> FilterConditionBuilder addNotIn(String columnName, String columnType, List<?> objects) {
        conditions.add(notIn(columnName, columnType, objects));
        return this;
    }

    public <T extends BaseObject> FilterConditionBuilder addNotIn(PropertyFunction<T, ?> function, List<?> objects) {
        String fieldName = AnnotationRetriever.getFieldName(function);
        String columnType = AnnotationRetriever.getFieldType(function);
        Map<String, FieldContent> map1 = AnnotationRetriever.getMappingFieldsMapCache(mappingClass);
        Optional.ofNullable(map1.get(fieldName)).map(f -> {
            conditions.add(notIn(f.getFieldName(), columnType, objects));
            return f;
        }).orElseGet(() -> {
            throw new ConfigurationIncorrectException("class " + mappingClass.getCanonicalName() + " can not parse");
        });
        return this;
    }

    public FilterCondition between(String columnName, String columnType, List<?> objects) {
        FilterCondition condition = new FilterCondition(columnName, columnType, Const.OPERATOR.BETWEEN);
        condition.setValues(objects);
        return condition;
    }

    public <T extends BaseObject> FilterConditionBuilder addBetween(String columnName, String columnType, List<?> objects) {
        conditions.add(between(columnName, columnType, objects));
        return this;
    }

    public <T extends BaseObject> FilterConditionBuilder addBetween(PropertyFunction<T, ?> function, List<?> objects) {
        String fieldName = AnnotationRetriever.getFieldName(function);
        String columnType = AnnotationRetriever.getFieldType(function);
        Map<String, FieldContent> map1 = AnnotationRetriever.getMappingFieldsMapCache(mappingClass);
        Optional.ofNullable(map1.get(fieldName)).map(f -> {
            conditions.add(between(f.getFieldName(), columnType, objects));
            return f;
        }).orElseGet(() -> {
            throw new ConfigurationIncorrectException("class " + mappingClass.getCanonicalName() + " can not parse");
        });
        return this;
    }

    public FilterCondition filter(String columnName, String columnType, Const.OPERATOR operator, Object object) {
        return new FilterCondition(columnName, columnType, operator, object);
    }

    public <T> FilterCondition filter(PropertyFunction<T, ?> function, Const.OPERATOR operator, Object object) {
        String fieldName = AnnotationRetriever.getFieldName(function);
        String columnType = AnnotationRetriever.getFieldType(function);
        Map<String, FieldContent> map1 = AnnotationRetriever.getMappingFieldsMapCache(mappingClass);
        return Optional.ofNullable(map1.get(fieldName)).map(f ->
                filter(f.getFieldName(), columnType, operator, object)
        ).orElseGet(() -> {
            throw new ConfigurationIncorrectException("class " + mappingClass.getCanonicalName() + " can not parse");
        });
    }

    public FilterConditionBuilder addFilter(String columnName, String columnType, Const.OPERATOR operator, Object object) {
        conditions.add(new FilterCondition(columnName, columnType, operator, object));
        return this;
    }

    public FilterConditionBuilder addFilter(String columnName, String columnType, Const.OPERATOR operator, List<?> values) {
        FilterCondition condition = new FilterCondition(columnName, columnType, operator);
        condition.setValues(values);
        conditions.add(condition);
        return this;
    }

    public FilterConditionBuilder addFilter(String columnName, Const.OPERATOR operator, List<?> values, String columnType) {
        FilterCondition condition = new FilterCondition(columnName, columnType, operator);
        condition.setValues(values);
        conditions.add(condition);
        return this;
    }

    public <T extends BaseObject> FilterConditionBuilder addFilter(PropertyFunction<T, ?> function, Const.OPERATOR operator, Object value) {
        String fieldName = AnnotationRetriever.getFieldName(function);
        String columnType = AnnotationRetriever.getFieldType(function);
        Map<String, FieldContent> map1 = AnnotationRetriever.getMappingFieldsMapCache(mappingClass);
        Optional.ofNullable(map1.get(fieldName)).map(f -> {
            FilterCondition condition = new FilterCondition(f.getFieldName(), operator);
            condition.setValue(value);
            condition.setColumnType(columnType);
            conditions.add(condition);
            return f;
        }).orElseGet(() -> {
            throw new ConfigurationIncorrectException("class " + mappingClass.getCanonicalName() + " can not parse");
        });
        return this;
    }

    public <T extends BaseObject> FilterConditionBuilder addFilter(PropertyFunction<T, ?> function, Const.OPERATOR operator, List<?> value) {
        String fieldName = AnnotationRetriever.getFieldName(function);
        String columnType = AnnotationRetriever.getFieldType(function);
        Map<String, FieldContent> map1 = AnnotationRetriever.getMappingFieldsMapCache(mappingClass);
        Optional.ofNullable(map1.get(fieldName)).map(f -> {
            FilterCondition condition = new FilterCondition(f.getFieldName(), operator);
            condition.setValues(value);
            condition.setColumnType(columnType);
            conditions.add(condition);
            return f;
        }).orElseGet(() -> {
            throw new ConfigurationIncorrectException("class " + mappingClass.getCanonicalName() + " can not parse");
        });
        return this;
    }

    public <T extends BaseObject> FilterConditionBuilder notNull(PropertyFunction<T, ?> function) {
        String fieldName = AnnotationRetriever.getFieldName(function);
        Map<String, FieldContent> map1 = AnnotationRetriever.getMappingFieldsMapCache(mappingClass);
        Optional.ofNullable(map1.get(fieldName)).map(f -> {
            FilterCondition condition = new FilterCondition(f.getFieldName(), Const.OPERATOR.NOTNULL);
            conditions.add(condition);
            return f;
        }).orElseGet(() -> {
            throw new ConfigurationIncorrectException("class " + mappingClass.getCanonicalName() + " can not parse");
        });
        return this;
    }

    public FilterConditionBuilder notNull(String columnName) {
        conditions.add(new FilterCondition(columnName, Const.OPERATOR.NOTNULL, null));
        return this;
    }

    public <T extends BaseObject> FilterConditionBuilder isNull(PropertyFunction<T, ?> function) {
        String fieldName = AnnotationRetriever.getFieldName(function);
        Map<String, FieldContent> map1 = AnnotationRetriever.getMappingFieldsMapCache(mappingClass);
        Optional.ofNullable(map1.get(fieldName)).map(f -> {
            FilterCondition condition = new FilterCondition(f.getFieldName(), Const.OPERATOR.NULL);
            conditions.add(condition);
            return f;
        }).orElseGet(() -> {
            throw new ConfigurationIncorrectException("class " + mappingClass.getCanonicalName() + " can not parse");
        });
        return this;
    }

    public FilterConditionBuilder isNull(String columnName) {
        conditions.add(new FilterCondition(columnName, Const.OPERATOR.NULL, null));
        return this;
    }

    public FilterConditionBuilder exists(FilterCondition condition) {
        FilterCondition condition1 = new FilterCondition("", Const.OPERATOR.EXISTS);
        condition1.setConditions(Arrays.stream(new FilterCondition[]{condition1}).collect(Collectors.toList()));
        conditions.add(condition1);
        return this;
    }

    public FilterConditionBuilder notExists(FilterCondition condition) {
        FilterCondition condition1 = new FilterCondition("", Const.OPERATOR.NOTEXIST);
        condition1.setConditions(Arrays.stream(new FilterCondition[]{condition1}).collect(Collectors.toList()));
        conditions.add(condition1);
        return this;
    }

    public FilterConditionBuilder not(FilterCondition condition) {
        FilterCondition condition1 = new FilterCondition("", Const.OPERATOR.NOT);
        condition1.setConditions(Arrays.stream(new FilterCondition[]{condition1}).collect(Collectors.toList()));
        conditions.add(condition1);
        return this;
    }


    public FilterConditionBuilder or(List<FilterCondition> objects) {
        conditions.add(new FilterCondition(mappingClass, Const.LINKOPERATOR.LINK_OR, objects));
        return this;
    }

    public FilterConditionBuilder addCondition(FilterCondition condition) {
        conditions.add(condition);
        return this;
    }

    public FilterCondition build() {
        FilterCondition condition = new FilterCondition(mappingClass, linkOper, conditions);
        if (!ObjectUtils.isEmpty(mappingClass)) {
            condition.setMappingClass(mappingClass);
        }
        return condition;
    }

    public List<FilterCondition> getConditions() {
        return conditions;
    }
}
