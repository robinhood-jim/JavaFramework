package com.robin.comm.calculator.math;

import com.robin.comm.calculator.AbstractCalculator;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.Assert;

import java.util.Map;

public class Add extends AbstractCalculator {
    public Add(){
        functionName="ADD";
    }
    public Add(DataCollectionMeta collectionMeta, String[] parameters) {
        super(collectionMeta, parameters);
        functionName="ADD";
        Assert.isTrue(parameters.length==2,"Add parameter must be two");
    }

    @Override
    public Pair<Const.CLASSTYPE, Object> doCalculator(Map<String, Object> inputObj) {
        setInput(inputObj);
        Const.CLASSTYPE metaType=getMetaTypeByPos(0);
        Object retValue=null;
        switch (metaType){
            case SHORT:
                retValue=Short.parseShort(getValue(parameters[0]).toString())+Short.parseShort(getValue(parameters[1]).toString());
                break;
            case INTEGER:
                retValue=Math.addExact(Integer.parseInt(getValue(parameters[0]).toString()),Integer.parseInt(getValue(parameters[1]).toString()));
                break;
            case LONG:
                retValue=Math.addExact(Long.parseLong(getValue(parameters[0]).toString()),Long.parseLong(getValue(parameters[1]).toString()));
                break;
            case FLOAT:
                retValue=Float.parseFloat(getValue(parameters[0]).toString())+Float.parseFloat(getValue(parameters[1]).toString());
                break;
            case DOUBLE:
                retValue=Double.parseDouble(getValue(parameters[0]).toString())+Double.parseDouble(getValue(parameters[1]).toString());
                break;
        }
        return Pair.of(metaType,retValue);
    }
    //public static String getFunctionName(){return "ADD";}
}
