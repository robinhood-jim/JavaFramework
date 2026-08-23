package com.robin.comm.calculator.utils;

import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Data
public class FunctionDefine{

    private String funcName;
    private List<ParameterDefine> parameters=new ArrayList<>();
    public String getUniqueName(){
        StringBuilder builder=new StringBuilder();
        builder.append(funcName).append(CalculatorParser.LEFTCROSS);
        if (!CollectionUtils.isEmpty(parameters))
        {
            for (ParameterDefine define :parameters)
            {
                builder.append(define.getParameterName()).append(',');
            }

            builder.delete(builder.length() - 1, builder.length());
        }
        builder.append(CalculatorParser.RIGHTCROSS);
        return builder.toString();
    }
    public FunctionDefine(String funcName){
        this.funcName=funcName;
    }

}