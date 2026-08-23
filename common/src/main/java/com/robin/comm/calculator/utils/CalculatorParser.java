package com.robin.comm.calculator.utils;

import cn.hutool.core.lang.Assert;
import com.robin.comm.calculator.CalculatorFactory;


public class CalculatorParser {
    public static final char LEFTCROSS='(';
    public static final char RIGHTCROSS=')';

    public static FunctionDefine parse(String functionCall){
        validateFunction(functionCall);
        int pos=functionCall.indexOf(LEFTCROSS);
        String functionName=functionCall.substring(0,pos);
        return parseFunction(functionName,pos,functionCall);
    }
    protected static FunctionDefine parseFunction(String funcName,int startPos,String input){
        StringBuilder stringBuilder=new StringBuilder();
        CalculatorFactory.functionExist(funcName);
        FunctionDefine funcDefine=new FunctionDefine(funcName);
        int enterCross=0;
        for(int i=startPos+1;i<input.length()-1;i++){
            if(input.charAt(i)==',' && enterCross==0){
                String parameter=stringBuilder.toString();
                ParameterDefine parameterDefine=wrapParameterDefine(parameter);
                funcDefine.getParameters().add(parameterDefine);
                stringBuilder.delete(0,stringBuilder.length());
            }else{
                if(input.charAt(i)==LEFTCROSS){
                    enterCross++;
                }else if(input.charAt(i)==RIGHTCROSS){
                    enterCross--;
                }
                stringBuilder.append(input.charAt(i));
            }
        }
        if(stringBuilder.length()>0){
            ParameterDefine parameterDefine=wrapParameterDefine(stringBuilder.toString());
            funcDefine.getParameters().add(parameterDefine);
        }
        return funcDefine;
    }
    private static ParameterDefine wrapParameterDefine(String parameter){
        ParameterDefine parameterDefine=new ParameterDefine();
        if(parameter.contains("(")){
            int pos=parameter.indexOf(LEFTCROSS);
            FunctionDefine functionDefine=parseFunction(parameter.substring(0,pos),pos,parameter);
            parameterDefine.setIfFunction(true);
            parameterDefine.setParameterName(parameter);
            parameterDefine.setSubFunction(functionDefine);
        }else{
            parameterDefine.setIfFunction(false);
            parameterDefine.setParameterName(parameter);
        }
        return parameterDefine;
    }

    private static void validateFunction(String function)
    {
        int encounterCross = 0;
        for (int i = 0; i < function.length(); i++)
        {
            if (function.charAt(i) == '(')
            {
                encounterCross++;
            }else if (function.charAt(i) == ')')
            {
                encounterCross--;
            }
        }
        Assert.isTrue(encounterCross == 0, "function enclosure not fit");
    }
}
