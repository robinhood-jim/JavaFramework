package com.robin.core.sql.util;

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
    public FilterConditionBuilder eq(String columnName, List<?> objects){
        conditions.add(new FilterCondition(columnName,Const.OPERATOR.EQ,objects));
        return this;
    }

    public List<FilterCondition> getConditions() {
        return conditions;
    }
}
