package com.robin.comm.calculator;

import com.robin.core.base.exception.MissingConfigException;
import com.robin.core.base.exception.ValueNullException;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractCalculator implements ICalculator {
    protected Map<String,Object> inputMap;
    protected Map<String,Object> calculateMap=new HashMap<>();
    protected Map<String, DataSetColumnMeta> columnMetaMap;
    protected String functionName;
    public AbstractCalculator(){

    }
    public AbstractCalculator(DataCollectionMeta collectionMeta){
        if( collectionMeta!=null && !CollectionUtils.isEmpty(collectionMeta.getColumnList())){
            columnMetaMap=collectionMeta.getColumnList().stream().collect(Collectors.toMap(DataSetColumnMeta::getColumnCode, Function.identity()));
        }
    }
    protected void setInput(Map<String,Object> input){
        inputMap=input;
    }
    public Object getValue(String parameterName){
        Object retVal=inputMap.getOrDefault(parameterName,null);
        if(retVal==null){
            retVal=calculateMap.getOrDefault(parameterName,null);
        }
        if(retVal==null){
            throw new ValueNullException("get value null!");
        }
        return retVal;
    }
    public void addParameterValue(String parameterName, Const.CLASSTYPE columnType, Object value){
        columnMetaMap.putIfAbsent(parameterName,new DataSetColumnMeta(parameterName,columnType));
        calculateMap.putIfAbsent(parameterName,value);

    }
    protected Const.CLASSTYPE getMetaTypeByPos(int pos,String[] parameters){
        if(pos<parameters.length){
            DataSetColumnMeta columnMeta=columnMetaMap.getOrDefault(parameters[pos],null);
            if(columnMeta!=null){
                return  columnMeta.getColumnType();
            }
        }
        throw new MissingConfigException("out of position");
    }
    public void clearTemp(){
        calculateMap.clear();
    }
    public String getFunctionName(){
        return functionName;
    }
}
