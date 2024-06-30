package com.robin.core.sql.util;

import com.robin.core.base.dao.util.AnnotationRetriever;
import com.robin.core.base.dao.util.PropertyFunction;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.util.Const;

import java.util.ArrayList;
import java.util.List;

public class FilterConditionBuilder {
    private List<FilterCondition> conditions=new ArrayList<>();
    public FilterConditionBuilder(){

    }
    public FilterConditionBuilder withCondition(FilterCondition filterCondition){
        conditions.add(filterCondition);
        return this;
    }
    public FilterConditionBuilder eq(String columnName, Object object){
        conditions.add(new FilterCondition(columnName, Const.OPERATOR.EQ,object));
        return this;
    }
    public FilterConditionBuilder eq(String columnName, Object object,String columnType){
        conditions.add(new FilterCondition(columnName, Const.OPERATOR.EQ,object,columnType));
        return this;
    }
    public <T extends BaseObject> FilterConditionBuilder eq(PropertyFunction<T,?> function, Object object){
        String fieldName=AnnotationRetriever.getFieldName(function);
        String columnType=AnnotationRetriever.getFieldType(function);
        conditions.add(new FilterCondition(fieldName, Const.OPERATOR.EQ,object,columnType));
        return this;
    }
    public <T extends BaseObject> FilterConditionBuilder eq(PropertyFunction<T,?> function, Object object,String columnType){
        String fieldName=AnnotationRetriever.getFieldName(function);
        conditions.add(new FilterCondition(fieldName, Const.OPERATOR.EQ,object,columnType));
        return this;
    }
    public <T extends BaseObject> FilterConditionBuilder in(String columnName, List<?> objects){
        FilterCondition condition=new FilterCondition(columnName, Const.OPERATOR.IN);
        condition.setValues(objects);
        conditions.add(condition);
        return this;
    }
    public <T extends BaseObject> FilterConditionBuilder in(String columnName, List<?> objects,String columnType){
        FilterCondition condition=new FilterCondition(columnName, Const.OPERATOR.IN);
        condition.setValues(objects);
        condition.setColumnType(columnType);
        conditions.add(condition);
        return this;
    }
    public <T extends BaseObject> FilterConditionBuilder in(PropertyFunction<T,?> function, List<?> objects){
        String fieldName=AnnotationRetriever.getFieldName(function);
        String columnType=AnnotationRetriever.getFieldType(function);
        FilterCondition condition=new FilterCondition(fieldName, Const.OPERATOR.IN);
        condition.setValues(objects);
        condition.setColumnType(columnType);
        conditions.add(condition);
        return this;
    }
    public <T extends BaseObject> FilterConditionBuilder notIn(String columnName, List<?> objects){
        FilterCondition condition=new FilterCondition(columnName, Const.OPERATOR.NOTIN);
        condition.setValues(objects);
        conditions.add(condition);
        return this;
    }
    public <T extends BaseObject> FilterConditionBuilder notIn(String columnName, List<?> objects,String columnType){
        FilterCondition condition=new FilterCondition(columnName, Const.OPERATOR.NOTIN);
        condition.setValues(objects);
        condition.setColumnType(columnType);
        conditions.add(condition);
        return this;
    }
    public <T extends BaseObject> FilterConditionBuilder ontIn(PropertyFunction<T,?> function, List<?> objects){
        String fieldName=AnnotationRetriever.getFieldName(function);
        String columnType=AnnotationRetriever.getFieldType(function);
        FilterCondition condition=new FilterCondition(fieldName, Const.OPERATOR.NOTIN);
        condition.setValues(objects);
        condition.setColumnType(columnType);
        conditions.add(condition);
        return this;
    }
    public <T extends BaseObject> FilterConditionBuilder between(String columnName, List<?> objects,String columnType){
        FilterCondition condition=new FilterCondition(columnName, Const.OPERATOR.BETWEEN);
        condition.setValues(objects);
        conditions.add(condition);
        return this;
    }
    public <T extends BaseObject> FilterConditionBuilder between(PropertyFunction<T,?> function, List<?> objects){
        String fieldName=AnnotationRetriever.getFieldName(function);
        String columnType=AnnotationRetriever.getFieldType(function);
        FilterCondition condition=new FilterCondition(fieldName, Const.OPERATOR.BETWEEN);
        condition.setValues(objects);
        condition.setColumnType(columnType);
        conditions.add(condition);
        return this;
    }
    public FilterConditionBuilder filter(String columnName,Const.OPERATOR operator, Object object){
        conditions.add(new FilterCondition(columnName, operator,object));
        return this;
    }
    public FilterConditionBuilder filter(String columnName,Const.OPERATOR operator, Object object,String columnType){
        conditions.add(new FilterCondition(columnName, operator,object,columnType));
        return this;
    }
    public FilterConditionBuilder filter(String columnName,Const.OPERATOR operator, List<?> values){
        conditions.add(new FilterCondition(columnName, operator,values));
        return this;
    }
    public FilterConditionBuilder filter(String columnName,Const.OPERATOR operator, List<?> values,String columnType){
        conditions.add(new FilterCondition(columnName, operator,values,columnType));
        return this;
    }
    public <T extends BaseObject> FilterConditionBuilder filter(PropertyFunction<T,?> function,Const.OPERATOR operator,Object value){
        String fieldName=AnnotationRetriever.getFieldName(function);
        String columnType=AnnotationRetriever.getFieldType(function);
        conditions.add(new FilterCondition(fieldName, operator,value,columnType));
        return this;
    }
    public <T extends BaseObject> FilterConditionBuilder filter(PropertyFunction<T,?> function,Const.OPERATOR operator,Object value,String columnType){
        String fieldName=AnnotationRetriever.getFieldName(function);
        conditions.add(new FilterCondition(fieldName, operator,value,columnType));
        return this;
    }
    public <T extends BaseObject> FilterConditionBuilder filter(PropertyFunction<T,?> function,Const.OPERATOR operator,List<?> value){
        String fieldName=AnnotationRetriever.getFieldName(function);
        conditions.add(new FilterCondition(fieldName, operator,value));
        return this;
    }
    public <T extends BaseObject> FilterConditionBuilder filter(PropertyFunction<T,?> function,Const.OPERATOR operator,List<?> value,String columnType){
        String fieldName=AnnotationRetriever.getFieldName(function);
        conditions.add(new FilterCondition(fieldName, operator,value,columnType));
        return this;
    }
    public <T extends BaseObject> FilterConditionBuilder notNull(PropertyFunction<T,?> function){
        String fieldName=AnnotationRetriever.getFieldName(function);
        conditions.add(new FilterCondition(fieldName, Const.OPERATOR.NOTNULL,null));
        return this;
    }
    public FilterConditionBuilder notNull(String columnName){
        conditions.add(new FilterCondition(columnName, Const.OPERATOR.NOTNULL,null));
        return this;
    }
    public <T extends BaseObject> FilterConditionBuilder isNull(PropertyFunction<T,?> function){
        String fieldName=AnnotationRetriever.getFieldName(function);
        conditions.add(new FilterCondition(fieldName, Const.OPERATOR.NULL,null));
        return this;
    }
    public FilterConditionBuilder isNull(String columnName){
        conditions.add(new FilterCondition(columnName, Const.OPERATOR.NULL,null));
        return this;
    }
    public FilterConditionBuilder exists(String value){
        conditions.add(new FilterCondition(Const.OPERATOR.EXISTS,value));
        return this;
    }
    public FilterConditionBuilder notExists(String value){
        conditions.add(new FilterCondition(Const.OPERATOR.NOTEXIST,value));
        return this;
    }
    public FilterConditionBuilder not(String value){
        conditions.add(new FilterCondition(Const.OPERATOR.NOT,value));
        return this;
    }



    public FilterConditionBuilder or(List<FilterCondition> objects){
        conditions.add(new FilterCondition(Const.OPERATOR.LINK_OR,objects));
        return this;
    }
    public FilterConditionBuilder addCondition(FilterCondition condition){
        conditions.add(condition);
        return this;
    }

    public List<FilterCondition> getConditions() {
        return conditions;
    }
}
