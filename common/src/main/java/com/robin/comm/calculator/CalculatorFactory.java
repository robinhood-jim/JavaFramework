package com.robin.comm.calculator;

import com.robin.comm.calculator.utils.FunctionDefine;
import com.robin.comm.calculator.utils.ParameterDefine;
import com.robin.core.base.exception.MissingConfigException;
import com.robin.core.base.exception.OperationNotSupportException;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class CalculatorFactory {
    protected  static  Map<String,Class<? extends ICalculator>> classMap=new HashMap<>();
    protected  static Map<String,Constructor<? extends ICalculator>> constructorMap=new HashMap<>();

    static {

        Iterator<ICalculator> iterator= ServiceLoader.load(ICalculator.class).iterator();
        try {
            while(iterator.hasNext()){
                ICalculator iCalculator=iterator.next();
                classMap.put(iCalculator.getFunctionName(),iCalculator.getClass());
                Constructor<? extends ICalculator> constructor1 = iCalculator.getClass().getConstructor(DataCollectionMeta.class);
                constructorMap.put(iCalculator.getFunctionName(), constructor1);
            }
        }catch (NoSuchMethodException ex){
            log.error("{}",ex.getMessage());
        }
    }


    public static ICalculator getCalculator(String funcName,DataCollectionMeta collectionMeta,String[] parameters){
        Class<? extends  ICalculator> clazz=classMap.getOrDefault(funcName.toUpperCase(),classMap.get(funcName.toLowerCase()));
        Constructor<? extends ICalculator> constructor=constructorMap.getOrDefault(funcName.toUpperCase(),constructorMap.get(funcName.toLowerCase()));
        try {
            if (clazz != null) {
                return constructor.newInstance(collectionMeta);
            }
            else {
                throw new MissingConfigException("funcName is missing");
            }
        }catch (Exception ex){
            throw new OperationNotSupportException(ex.getMessage());
        }
    }
    public static void functionExist(String functionName){
        Class<? extends ICalculator> clazz=classMap.getOrDefault(functionName.toUpperCase(),classMap.get(functionName.toLowerCase()));
        if(clazz==null){
            throw new MissingConfigException("function name "+functionName+" does not defined!");
        }
    }
    public static Pair<Const.CLASSTYPE,Object> doCalculate(DataCollectionMeta collectionMeta, Map<String,Object> input, FunctionDefine functionDefine, ConcurrentHashMap<String,ICalculator> calculatorMap){
        List<String> parameters=functionDefine.getParameters().stream().map(ParameterDefine::getParameterName).collect(Collectors.toList());
        String uniqueName=functionDefine.getUniqueName();
        ICalculator calculator=calculatorMap.computeIfAbsent(uniqueName,(key)-> getCalculator(functionDefine.getFuncName(),collectionMeta,parameters.toArray(new String[0])));
        calculator.clearTemp();
        for(ParameterDefine parameterDefine: functionDefine.getParameters()){
            if(parameterDefine.isIfFunction()){
                Pair<Const.CLASSTYPE,Object> pair=doCalculate(collectionMeta,input,parameterDefine.getSubFunction(),calculatorMap);
                calculator.addParameterValue(parameterDefine.getParameterName(),pair.getKey(),pair.getValue());
            }
        }
        return calculator.doCalculator(input,parameters.toArray(new String[0]));
    }
}
