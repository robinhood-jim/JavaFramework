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
    public <T extends BaseObject> FilterConditionBuilder eq(PropertyFunction<T,?> function, Object object){
        String fieldName=AnnotationRetriever.getFieldName(function);
        conditions.add(new FilterCondition(fieldName, Const.OPERATOR.EQ,object));
        return this;
    }
    public FilterConditionBuilder filter(String columnName,Const.OPERATOR operator, Object object){
        conditions.add(new FilterCondition(columnName, operator,object));
        return this;
    }
    public FilterConditionBuilder filter(String columnName,Const.OPERATOR operator, List<?> values){
        conditions.add(new FilterCondition(columnName, operator,values));
        return this;
    }
    public <T extends BaseObject> FilterConditionBuilder filter(PropertyFunction<T,?> function,Const.OPERATOR operator,Object value){
        String fieldName=AnnotationRetriever.getFieldName(function);
        conditions.add(new FilterCondition(fieldName, operator,value));
        return this;
    }
    public <T extends BaseObject> FilterConditionBuilder filter(PropertyFunction<T,?> function,Const.OPERATOR operator,List<?> value){
        String fieldName=AnnotationRetriever.getFieldName(function);
        conditions.add(new FilterCondition(fieldName, operator,value));
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


    public FilterConditionBuilder or(List<?> objects){
        conditions.add(new FilterCondition(Const.OPERATOR.LINK_OR,objects));
        return this;
    }

    public List<FilterCondition> getConditions() {
        return conditions;
    }
}
