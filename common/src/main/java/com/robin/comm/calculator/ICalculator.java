package com.robin.comm.calculator;

import com.robin.core.base.util.Const;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

public interface ICalculator {
    Pair<Const.CLASSTYPE,Object> doCalculator(Map<String,Object> inputObj);
    String getFunctionName();
    void clearTemp();
    Object getValue(String parameterName);
    void addParameterValue(String parameterName, Const.CLASSTYPE columnType, Object value);
}
