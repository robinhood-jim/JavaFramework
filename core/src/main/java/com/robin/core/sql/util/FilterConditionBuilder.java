package com.robin.core.sql.util;

import com.robin.core.base.dao.util.AnnotationRetriever;
import com.robin.core.base.dao.util.FieldContent;
import com.robin.core.base.dao.util.PropertyFunction;
import com.robin.core.base.exception.ConfigurationIncorrectException;
import com.robin.core.base.exception.MissingConfigException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.util.Const;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class FilterConditionBuilder {
    private List<FilterCondition> conditions = new ArrayList<>();

    private Const.LINKOPERATOR linkOper = Const.LINKOPERATOR.LINK_AND;
    private Map<Class<? extends BaseObject>,String> aliasMap;
    private SqlBuilder sqlBuilder;

    //private Class<? extends BaseObject> mappingClass;
    public FilterConditionBuilder(){

    }
    public FilterConditionBuilder sqlBuilder(SqlBuilder sqlBuilder){
        this.sqlBuilder=sqlBuilder;
        return this;
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
    public FilterConditionBuilder aliasMap(Map<Class<? extends BaseObject>,String> aliasMap){
        this.aliasMap=aliasMap;
        return this;
    }

    public <L extends BaseObject,R extends BaseObject> FilterCondition eq(Object left,Object right){
        FilterCondition condition = new FilterCondition(left, Const.OPERATOR.EQ);
        condition.setAliasMap(aliasMap);
        condition.setValue(right);
        return condition;
    }
    public <T extends BaseObject> FilterCondition eq(PropertyFunction<T, ?> function, Object object) {
        String fieldName = AnnotationRetriever.getFieldName(function);
        Class<T> mappingClass=AnnotationRetriever.getFieldOwnedClass(function);
        Map<String, FieldContent> map1 = AnnotationRetriever.getMappingFieldsMapCache(mappingClass);
        String columnType = AnnotationRetriever.getFieldType(function);
        return Optional.ofNullable(map1.get(fieldName)).map(f -> {
            FilterCondition condition = constructCondition(f.getFieldName(),columnType, Const.OPERATOR.EQ,mappingClass,object);
            return condition;
        }).orElseThrow(() -> new ConfigurationIncorrectException("class " + mappingClass.getCanonicalName() + " can not parse"));
    }
    public <T extends BaseObject,R extends BaseObject> FilterCondition eq(PropertyFunction<T, ?> left, PropertyFunction<R,?> right) {
        String fieldName = AnnotationRetriever.getFieldName(left);
        Class<T> mappingClass=AnnotationRetriever.getFieldOwnedClass(left);
        Map<String, FieldContent> map1 = AnnotationRetriever.getMappingFieldsMapCache(mappingClass);
        String columnType = AnnotationRetriever.getFieldType(left);
        return Optional.ofNullable(map1.get(fieldName)).map(f -> {
            FilterCondition condition = constructCondition(f.getFieldName(),columnType, Const.OPERATOR.EQ,mappingClass,right);
            return condition;
        }).orElseThrow(() -> new ConfigurationIncorrectException("class " + mappingClass.getCanonicalName() + " can not parse"));

    }

    public <T extends BaseObject> FilterConditionBuilder addEq(PropertyFunction<T, ?> function, Object object) {
        String fieldName = AnnotationRetriever.getFieldName(function);
        Class<T> mappingClass=AnnotationRetriever.getFieldOwnedClass(function);
        Map<String, FieldContent> map1 = AnnotationRetriever.getMappingFieldsMapCache(mappingClass);
        String columnType = AnnotationRetriever.getFieldType(function);
        Optional.ofNullable(map1.get(fieldName)).map(f -> {
            FilterCondition condition = constructCondition(f.getFieldName(),columnType, Const.OPERATOR.EQ,mappingClass,object);
            conditions.add(condition);
            return f;
        }).orElseThrow(() ->new ConfigurationIncorrectException("class " + mappingClass.getCanonicalName() + " field "+fieldName+" can not parse"));
        return this;
    }
    public <T extends BaseObject> FilterConditionBuilder addEq(Class<T> mappingClass,String fieldName, Object object) {
        Map<String, FieldContent> map1 = AnnotationRetriever.getMappingFieldsMapCache(mappingClass);
        Optional.ofNullable(map1.get(fieldName)).map(f -> {
            String columnType = AnnotationRetriever.getFieldType(map1.get(fieldName));
            FilterCondition condition = constructCondition(f.getFieldName(),columnType, Const.OPERATOR.EQ,mappingClass,object);
            conditions.add(condition);
            return f;
        }).orElseThrow(()->new ConfigurationIncorrectException("class " + mappingClass.getCanonicalName() + " can not parse"));
        return this;
    }
    private <T extends BaseObject> FilterCondition constructCondition(String fieldName,String columnType,Const.OPERATOR operator,Class<T> mappingClass,Object object){
        FilterCondition condition = new FilterCondition(fieldName, operator);
        condition.setMappingClass(mappingClass);
        condition.setValue(object);
        if(sqlBuilder!=null){
            condition.setSqlBuilder(sqlBuilder);
        }
        if(aliasMap!=null){
            condition.setAliasMap(aliasMap);
        }
        condition.setColumnType(columnType);
        return condition;
    }

    public <T extends BaseObject> FilterConditionBuilder addIn(String columnName, String columnType, List<?> objects) {
        conditions.add(in(columnName, columnType, objects));
        return this;
    }

    public FilterCondition in(String columnName, String columnType, List<?> objects) {
        FilterCondition condition = new FilterCondition(columnName, Const.OPERATOR.IN);
        condition.setValues(objects);
        condition.setAliasMap(aliasMap);
        condition.setAliasMap(aliasMap);
        condition.setColumnType(columnType);
        if(sqlBuilder!=null){
            condition.setSqlBuilder(sqlBuilder);
        }
        return condition;
    }
    public <T extends BaseObject> FilterCondition in(PropertyFunction<T,?> function, List<?> objects) {
        String fieldName = AnnotationRetriever.getFieldName(function);
        String columnType = AnnotationRetriever.getFieldType(function);
        Class<T> mappingClass=AnnotationRetriever.getFieldOwnedClass(function);
        Map<String, FieldContent> map1 = AnnotationRetriever.getMappingFieldsMapCache(mappingClass);
        String columnName;
        if(map1.containsKey(fieldName)){
            columnName=map1.get(fieldName).getFieldName();
        }else{
            throw new MissingConfigException("field not found in  "+fieldName);
        }
        FilterCondition condition = new FilterCondition(columnName, Const.OPERATOR.IN);
        condition.setValues(objects);
        condition.setAliasMap(aliasMap);
        condition.setColumnType(columnType);
        if(sqlBuilder!=null){
            condition.setSqlBuilder(sqlBuilder);
        }
        return condition;
    }
    public FilterCondition in(String columnName,FilterCondition inClause){
        FilterCondition condition = new FilterCondition(columnName, Const.OPERATOR.IN);
        condition.setAliasMap(aliasMap);
        if(sqlBuilder!=null){
            condition.setSqlBuilder(sqlBuilder);
        }
        condition.setConditions(Arrays.stream(new FilterCondition[]{inClause}).collect(Collectors.toList()));
        return condition;
    }

    public <T extends BaseObject> FilterConditionBuilder addIn(String columnName, List<?> objects, String columnType) {
        FilterCondition condition = new FilterCondition(columnName, Const.OPERATOR.IN);
        condition.setValues(objects);
        condition.setColumnType(columnType);
        condition.setAliasMap(aliasMap);
        conditions.add(condition);
        return this;
    }

    public <T extends BaseObject> FilterConditionBuilder addIn(PropertyFunction<T, ?> function,  List<?> objects) {
        String fieldName = AnnotationRetriever.getFieldName(function);
        Class<T> mappingClass=AnnotationRetriever.getFieldOwnedClass(function);
        String columnType = AnnotationRetriever.getFieldType(function);
        Map<String, FieldContent> map1 = AnnotationRetriever.getMappingFieldsMapCache(mappingClass);
        Optional.ofNullable(map1.get(fieldName)).map(f -> {
            conditions.add(in(f.getFieldName(), columnType, objects));
            return f;
        }).orElseThrow(() ->
                new ConfigurationIncorrectException("class " + mappingClass.getCanonicalName() + " can not parse"));
        return this;
    }

    public FilterCondition notIn(String columnName, String columnType, List<?> objects) {
        FilterCondition condition = new FilterCondition(columnName, Const.OPERATOR.NOTIN);
        condition.setValues(objects);
        condition.setAliasMap(aliasMap);
        condition.setColumnType(columnType);
        if(sqlBuilder!=null){
            condition.setSqlBuilder(sqlBuilder);
        }
        return condition;
    }
    public FilterCondition notIn(String columnName, FilterCondition clause) {
        FilterCondition condition = new FilterCondition(columnName, Const.OPERATOR.NOTIN);
        condition.setAliasMap(aliasMap);
        if(sqlBuilder!=null){
            condition.setSqlBuilder(sqlBuilder);
        }
        condition.setConditions(Arrays.stream(new FilterCondition[]{clause}).collect(Collectors.toList()));
        return condition;
    }

    public <T extends BaseObject> FilterConditionBuilder addIn(PropertyFunction<T, ?> function,FilterCondition inClause){
        String fieldName = AnnotationRetriever.getFieldName(function);
        String columnType = AnnotationRetriever.getFieldType(function);
        Class<T> mappingClass=AnnotationRetriever.getFieldOwnedClass(function);
        Map<String, FieldContent> map1 = AnnotationRetriever.getMappingFieldsMapCache(mappingClass);
        Optional.ofNullable(map1.get(fieldName)).map(f -> {
            conditions.add(in(f.getFieldName(), inClause));
            return f;
        }).orElseThrow(() ->
            new ConfigurationIncorrectException("class " + mappingClass.getCanonicalName() + " can not parse"));
        return this;
    }
    public <T extends BaseObject> FilterConditionBuilder addNotIn(PropertyFunction<T, ?> function,FilterCondition inClause){
        String fieldName = AnnotationRetriever.getFieldName(function);
        Class<T> mappingClass=AnnotationRetriever.getFieldOwnedClass(function);
        Map<String, FieldContent> map1 = AnnotationRetriever.getMappingFieldsMapCache(mappingClass);
        Optional.ofNullable(map1.get(fieldName)).map(f -> {
            conditions.add(notIn(f.getFieldName(), inClause));
            return f;
        }).orElseThrow(() ->
            new ConfigurationIncorrectException("class " + mappingClass.getCanonicalName() + " can not parse"));
        return this;
    }

    public <T extends BaseObject> FilterConditionBuilder addNotIn(String columnName, String columnType, List<?> objects) {
        conditions.add(notIn(columnName, columnType, objects));
        return this;
    }

    public <T extends BaseObject> FilterConditionBuilder addNotIn(PropertyFunction<T, ?> function, List<?> objects) {
        String fieldName = AnnotationRetriever.getFieldName(function);
        String columnType = AnnotationRetriever.getFieldType(function);
        Class<T> mappingClass=AnnotationRetriever.getFieldOwnedClass(function);
        Map<String, FieldContent> map1 = AnnotationRetriever.getMappingFieldsMapCache(mappingClass);
        Optional.ofNullable(map1.get(fieldName)).map(f -> {
            conditions.add(notIn(f.getFieldName(), columnType, objects));
            return f;
        }).orElseThrow(() ->
             new ConfigurationIncorrectException("class " + mappingClass.getCanonicalName() + " can not parse"));
        return this;
    }

    public FilterCondition between(String columnName, String columnType, List<?> objects) {
        FilterCondition condition = new FilterCondition(columnName, columnType, Const.OPERATOR.BETWEEN);
        condition.setAliasMap(aliasMap);
        condition.setValues(objects);
        if(sqlBuilder!=null){
            condition.setSqlBuilder(sqlBuilder);
        }
        return condition;
    }

    public <T extends BaseObject> FilterConditionBuilder addBetween(String columnName, String columnType, List<?> objects) {
        conditions.add(between(columnName, columnType, objects));
        return this;
    }

    public <T extends BaseObject> FilterConditionBuilder addBetween(PropertyFunction<T, ?> function, List<?> objects) {
        String fieldName = AnnotationRetriever.getFieldName(function);
        String columnType = AnnotationRetriever.getFieldType(function);
        Class<T> mappingClass=AnnotationRetriever.getFieldOwnedClass(function);
        Map<String, FieldContent> map1 = AnnotationRetriever.getMappingFieldsMapCache(mappingClass);
        Optional.ofNullable(map1.get(fieldName)).map(f -> {
            conditions.add(between(f.getFieldName(), columnType, objects));
            return f;
        }).orElseThrow(() ->
                new ConfigurationIncorrectException("class " + mappingClass.getCanonicalName() + " can not parse"));
        return this;
    }

    public FilterCondition filter(String columnName, String columnType, Const.OPERATOR operator, Object object) {
        FilterCondition condition= new FilterCondition(columnName, columnType, operator, object);
        if(sqlBuilder!=null){
            condition.setSqlBuilder(sqlBuilder);
        }
        return condition;
    }
    public FilterCondition filter(Object left, Const.OPERATOR operator, Object right) {
        FilterCondition condition=new FilterCondition(left,operator);
        if(sqlBuilder!=null){
            condition.setSqlBuilder(sqlBuilder);
        }
        condition.setValue(right);
        return condition;
    }

    public <T extends BaseObject> FilterCondition filter(PropertyFunction<T, ?> function, Const.OPERATOR operator, Object object) {
        String fieldName = AnnotationRetriever.getFieldName(function);
        String columnType = AnnotationRetriever.getFieldType(function);
        Class<T> mappingClass=AnnotationRetriever.getFieldOwnedClass(function);
        Map<String, FieldContent> map1 = AnnotationRetriever.getMappingFieldsMapCache(mappingClass);
        return Optional.ofNullable(map1.get(fieldName)).map(f ->
                filter(f.getFieldName(), columnType, operator, object)
        ).orElseThrow(() ->
                new ConfigurationIncorrectException("class " + mappingClass.getCanonicalName() + " can not parse"));
    }
    public <T extends BaseObject,R extends BaseObject> FilterCondition filter(PropertyFunction<T, ?> function, Const.OPERATOR operator, PropertyFunction<R,?> object) {
        String fieldName = AnnotationRetriever.getFieldName(function);
        String columnType = AnnotationRetriever.getFieldType(function);
        Class<T> mappingClass=AnnotationRetriever.getFieldOwnedClass(function);
        Map<String, FieldContent> map1 = AnnotationRetriever.getMappingFieldsMapCache(mappingClass);
        return Optional.ofNullable(map1.get(fieldName)).map(f ->
                filter(f.getFieldName(), columnType, operator, object)
        ).orElseThrow(() ->
                new ConfigurationIncorrectException("class " + mappingClass.getCanonicalName() + " can not parse"));
    }

    public FilterConditionBuilder addFilter(String columnName, String columnType, Const.OPERATOR operator, Object object) {
        FilterCondition condition=new FilterCondition(columnName, columnType, operator, object);
        if(sqlBuilder!=null){
            condition.setSqlBuilder(sqlBuilder);
        }
        conditions.add(condition);
        return this;
    }
    public FilterConditionBuilder addFilter(Object left,Object right,Const.OPERATOR operator){
        FilterCondition condition=new FilterCondition(left,operator);
        condition.setValue(right);
        if(sqlBuilder!=null){
            condition.setSqlBuilder(sqlBuilder);
        }
        return this;
    }

    public FilterConditionBuilder addFilter(String columnName, String columnType, Const.OPERATOR operator, List<?> values) {
        FilterCondition condition = new FilterCondition(columnName, columnType, operator);
        condition.setValues(values);
        condition.setAliasMap(aliasMap);
        if(sqlBuilder!=null){
            condition.setSqlBuilder(sqlBuilder);
        }
        conditions.add(condition);
        return this;
    }

    public FilterConditionBuilder addFilter(String columnName, Const.OPERATOR operator, List<?> values, String columnType) {
        FilterCondition condition = new FilterCondition(columnName, columnType, operator);
        condition.setValues(values);
        condition.setAliasMap(aliasMap);
        if(sqlBuilder!=null){
            condition.setSqlBuilder(sqlBuilder);
        }
        conditions.add(condition);
        return this;
    }
    public <T extends BaseObject> FilterConditionBuilder addFilter(Class<T> mappingClass, String propertyName, Const.OPERATOR operator, List<?> values, String columnType) {
        Map<String, FieldContent> map1 = AnnotationRetriever.getMappingFieldsMapCache(mappingClass);
        if(map1.containsKey(propertyName)) {
            FilterCondition condition = new FilterCondition(map1.get(propertyName).getFieldName(), columnType, operator);
            condition.setValues(values);
            condition.setAliasMap(aliasMap);
            if(sqlBuilder!=null){
                condition.setSqlBuilder(sqlBuilder);
            }
            conditions.add(condition);
        }
        return this;
    }


    public <T extends BaseObject> FilterConditionBuilder addFilter(PropertyFunction<T, ?> function, Const.OPERATOR operator, Object value) {
        String fieldName = AnnotationRetriever.getFieldName(function);
        String columnType = AnnotationRetriever.getFieldType(function);
        Class<T> mappingClass=AnnotationRetriever.getFieldOwnedClass(function);
        Map<String, FieldContent> map1 = AnnotationRetriever.getMappingFieldsMapCache(mappingClass);
        Optional.ofNullable(map1.get(fieldName)).map(f -> {
            FilterCondition condition = constructCondition(f.getFieldName(),columnType, operator,mappingClass,value);
            return f;
        }).orElseThrow(() ->
                new ConfigurationIncorrectException("class " + mappingClass.getCanonicalName() + " can not parse"));
        return this;
    }

    public <T extends BaseObject> FilterConditionBuilder addFilter(PropertyFunction<T, ?> function, Const.OPERATOR operator, List<?> value) {
        String fieldName = AnnotationRetriever.getFieldName(function);
        String columnType = AnnotationRetriever.getFieldType(function);
        Class<T> mappingClass=AnnotationRetriever.getFieldOwnedClass(function);
        Map<String, FieldContent> map1 = AnnotationRetriever.getMappingFieldsMapCache(mappingClass);
        Optional.ofNullable(map1.get(fieldName)).map(f -> {
            FilterCondition condition = constructCondition(f.getFieldName(),columnType, operator,mappingClass,value);
            conditions.add(condition);
            return f;
        }).orElseThrow(() ->
                new ConfigurationIncorrectException("class " + mappingClass.getCanonicalName() + " can not parse"));
        return this;
    }

    public <T extends BaseObject> FilterConditionBuilder notNull(PropertyFunction<T, ?> function) {
        String fieldName = AnnotationRetriever.getFieldName(function);
        Class<T> mappingClass=AnnotationRetriever.getFieldOwnedClass(function);
        Map<String, FieldContent> map1 = AnnotationRetriever.getMappingFieldsMapCache(mappingClass);
        Optional.ofNullable(map1.get(fieldName)).map(f -> {
            FilterCondition condition = new FilterCondition(f.getFieldName(), Const.OPERATOR.NOTNULL);
            conditions.add(condition);
            return f;
        }).orElseThrow(() ->
                new ConfigurationIncorrectException("class " + mappingClass.getCanonicalName() + " can not parse"));
        return this;
    }

    public FilterConditionBuilder notNull(String columnName) {
        conditions.add(new FilterCondition(columnName, Const.OPERATOR.NOTNULL, null));
        return this;
    }

    public <T extends BaseObject> FilterConditionBuilder isNull(PropertyFunction<T, ?> function) {
        String fieldName = AnnotationRetriever.getFieldName(function);
        Class<T> mappingClass=AnnotationRetriever.getFieldOwnedClass(function);
        Map<String, FieldContent> map1 = AnnotationRetriever.getMappingFieldsMapCache(mappingClass);
        Optional.ofNullable(map1.get(fieldName)).map(f -> {
            FilterCondition condition = new FilterCondition(f.getFieldName(), Const.OPERATOR.NULL);
            condition.setAliasMap(aliasMap);
            conditions.add(condition);
            return f;
        }).orElseThrow(() ->
                new ConfigurationIncorrectException("class " + mappingClass.getCanonicalName() + " can not parse"));
        return this;
    }

    public FilterConditionBuilder isNull(String columnName) {
        FilterCondition condition=new FilterCondition(columnName, Const.OPERATOR.NULL, null);
        condition.setAliasMap(aliasMap);
        conditions.add(condition);
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


    public FilterConditionBuilder or(Class<? extends BaseObject> mappingClass,List<FilterCondition> objects) {
        conditions.add(new FilterCondition(mappingClass, Const.LINKOPERATOR.LINK_OR, objects));
        return this;
    }

    public FilterConditionBuilder addCondition(FilterCondition condition) {
        conditions.add(condition);
        return this;
    }

    public FilterCondition build() {
        FilterCondition condition = new FilterCondition(linkOper, conditions);
        return condition;
    }

    public List<FilterCondition> getConditions() {
        return conditions;
    }
}
