package com.robin.core.sql.util;

import java.util.ArrayList;
import java.util.List;

public class FilterConditions {
    private List<FilterCondition> conditions=new ArrayList<>();
    public FilterConditions(){

    }
    public FilterConditions withCondition(FilterCondition filterCondition){
        conditions.add(filterCondition);
        return this;
    }

    public List<FilterCondition> getConditions() {
        return conditions;
    }
}
